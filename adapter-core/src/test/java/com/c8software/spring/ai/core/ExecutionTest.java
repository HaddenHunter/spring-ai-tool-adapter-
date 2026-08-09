package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolIdempotent;
import com.c8software.spring.ai.core.annotation.AiToolRiskLevel;
import com.c8software.spring.ai.core.annotation.AiToolSensitive;
import com.c8software.spring.ai.core.annotation.AiToolVisibility;
import com.c8software.spring.ai.core.annotation.RiskLevel;
import com.c8software.spring.ai.core.annotation.SensitiveType;
import com.c8software.spring.ai.core.annotation.ToolVisibility;
import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditRecord;
import com.c8software.spring.ai.core.approval.ApprovalDecision;
import com.c8software.spring.ai.core.approval.ToolApprovalManager;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.context.ConversationContextHolder;
import com.c8software.spring.ai.core.context.DefaultConversationSession;
import com.c8software.spring.ai.core.context.DefaultUserChoiceTracker;
import com.c8software.spring.ai.core.context.TaskContext;
import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolResult;
import com.c8software.spring.ai.core.idempotency.DefaultIdempotencyKeyResolver;
import com.c8software.spring.ai.core.idempotency.InMemoryIdempotencyStore;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.DefaultPermissionChecker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.c8software.spring.ai.core.visibility.DefaultToolVisibilityFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionTest {

    @Test
    void executesRegisteredTool() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new DemoTools(), "demoTools");
        DefaultToolExecutor executor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                new SyncAuditLogger(),
                new ObjectMapper()
        );

        ToolResult result = executor.execute(
                "hello",
                "{\"name\":\"Ada\"}",
                new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now())
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("Hello Ada");
    }

    @Test
    void auditsContextSnapshotsAroundToolExecution() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new DemoTools(), "demoTools");
        SyncAuditLogger auditLogger = new SyncAuditLogger();
        DefaultToolExecutor executor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                auditLogger,
                new ObjectMapper()
        );
        TaskContext taskContext = new TaskContext();
        taskContext.setTaskId("task-1");
        taskContext.setTaskType("send_sms");
        new DefaultUserChoiceTracker().confirmChoice(taskContext, "selectedCustomerId", "1001", "user-confirmed");
        ConversationContextHolder.bind(
                new DefaultConversationSession("session-1", "tenant", "tester", "ops", "openai", "gpt", Instant.now()),
                taskContext
        );

        try {
            executor.execute(
                    "hello",
                    "{\"name\":\"Ada\"}",
                    new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now())
            );
        } finally {
            ConversationContextHolder.clear();
        }

        assertThat(auditLogger.recent()).hasSize(1);
        AuditRecord record = auditLogger.recent().get(0);
        assertThat(record.getEventType()).isEqualTo("TOOL_CALL");
        assertThat(record.getContextBeforeHash()).isNotBlank();
        assertThat(record.getContextAfterHash()).isNotBlank();
    }

    @Test
    void rejectsHighRiskToolWhenApprovalIsDenied() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new DemoTools(), "demoTools");
        DefaultToolExecutor executor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                new SyncAuditLogger(),
                new ObjectMapper(),
                new AiToolProperties(),
                new RejectingApprovalManager(),
                new InMemoryIdempotencyStore(),
                new DefaultIdempotencyKeyResolver()
        );

        assertThatThrownBy(() -> executor.execute(
                "danger",
                "{\"name\":\"Ada\"}",
                new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now())
        )).isInstanceOf(AiToolExecutionException.class)
                .hasMessageContaining("approval rejected");
    }

    @Test
    void idempotentToolReturnsCachedResultWithoutRepeatingBusinessCall() {
        ToolRegistry registry = new ToolRegistry();
        DemoTools tools = new DemoTools();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(tools, "demoTools");
        DefaultToolExecutor executor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                new SyncAuditLogger(),
                new ObjectMapper()
        );
        ExecutionContext context = new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now());

        ToolResult first = executor.execute("once", "{\"name\":\"Ada\"}", context);
        ToolResult second = executor.execute("once", "{\"name\":\"Ada\"}", context);

        assertThat(first.getData()).isEqualTo("call-1-Ada");
        assertThat(second.getData()).isEqualTo("call-1-Ada");
        assertThat(tools.onceCalls).isEqualTo(1);
    }

    @Test
    void timesOutToolExecutionInIsolatedWorker() {
        ToolRegistry registry = new ToolRegistry();
        AiToolProperties properties = new AiToolProperties();
        properties.setDefaultTimeoutMillis(50L);
        new AiToolRegistrar(registry, properties)
                .postProcessAfterInitialization(new DemoTools(), "demoTools");
        DefaultToolExecutor executor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                new SyncAuditLogger(),
                new ObjectMapper(),
                properties
        );

        assertThatThrownBy(() -> executor.execute(
                "slow",
                "{}",
                new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now())
        )).isInstanceOf(AiToolExecutionException.class)
                .hasMessageContaining("timed out");
    }

    @Test
    void masksSensitiveReturnValueBeforeReturningResult() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new DemoTools(), "demoTools");
        DefaultToolExecutor executor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                new SyncAuditLogger(),
                new ObjectMapper()
        );

        ToolResult result = executor.execute(
                "return_mobile",
                "{}",
                new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now())
        );

        assertThat(result.getData()).isEqualTo("138****5678");
    }

    @Test
    void visibilityFilterHidesInternalToolsWithoutInternalPermission() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new DemoTools(), "demoTools");
        DefaultToolVisibilityFilter filter = new DefaultToolVisibilityFilter();
        ExecutionContext publicContext = new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now());
        ExecutionContext internalContext = new ExecutionContext("tester", "tenant", "trace",
                Collections.singleton("tool:internal"), Instant.now());

        assertThat(filter.filter(registry.listAll(), publicContext))
                .extracting(ToolDefinition::getName)
                .doesNotContain("internal_tool");
        assertThat(filter.filter(registry.listAll(), internalContext))
                .extracting(ToolDefinition::getName)
                .contains("internal_tool");
    }

    static class DemoTools {
        private int onceCalls;

        @AiTool(name = "hello", description = "hello", paramDescriptions = "name")
        public String hello(String name) {
            return "Hello " + name;
        }

        @AiTool(name = "danger", description = "danger", paramDescriptions = "name")
        @AiToolRiskLevel(RiskLevel.HIGH)
        public String danger(String name) {
            return "Danger " + name;
        }

        @AiTool(name = "once", description = "once", paramDescriptions = "name")
        @AiToolIdempotent(key = "#name")
        public String once(String name) {
            onceCalls++;
            return "call-" + onceCalls + "-" + name;
        }

        @AiTool(name = "internal_tool", description = "internal")
        @AiToolVisibility(ToolVisibility.INTERNAL)
        public String internalTool() {
            return "internal";
        }

        @AiTool(name = "slow", description = "slow")
        public String slow() throws InterruptedException {
            Thread.sleep(500L);
            return "late";
        }

        @AiTool(name = "return_mobile", description = "return mobile")
        @AiToolSensitive(type = SensitiveType.MOBILE)
        public String returnMobile() {
            return "13812345678";
        }
    }

    static class RejectingApprovalManager implements ToolApprovalManager {
        public ApprovalDecision approve(ToolDefinition definition, ExecutionContext context, String maskedInput) {
            return ApprovalDecision.rejected("REJECTED", "approval rejected");
        }
    }

    static class SyncAuditLogger implements AuditLogger {
        private final List<AuditRecord> records = new ArrayList<AuditRecord>();

        public void log(AuditRecord record) {
            records.add(record);
        }

        public List<AuditRecord> recent() {
            return records;
        }
    }
}
