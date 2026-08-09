package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditRecord;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.context.ConversationContextHolder;
import com.c8software.spring.ai.core.context.DefaultConversationSession;
import com.c8software.spring.ai.core.context.DefaultUserChoiceTracker;
import com.c8software.spring.ai.core.context.TaskContext;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolResult;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.DefaultPermissionChecker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    static class DemoTools {

        @AiTool(name = "hello", description = "hello", paramDescriptions = "name")
        public String hello(String name) {
            return "Hello " + name;
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
