package com.c8software.spring.ai.springai;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.audit.AsyncAuditLogger;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.context.ContextSnapshot;
import com.c8software.spring.ai.core.context.ConversationContextHolder;
import com.c8software.spring.ai.core.context.DefaultConversationSession;
import com.c8software.spring.ai.core.context.TaskContext;
import com.c8software.spring.ai.core.context.TaskStatus;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.DefaultPermissionChecker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringAiToolCallbackTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void exposesRegistryAsSpringAiCallbacks() throws Exception {
        ToolRegistry registry = new ToolRegistry();
        AiToolRegistrar processor = new AiToolRegistrar(registry, new AiToolProperties());
        processor.postProcessAfterInitialization(new AccountTools(), "accountTools");

        ToolExecutor executor = new DefaultToolExecutor(registry, new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(), new AsyncAuditLogger(), objectMapper);
        GovernedToolCallbackProvider provider = new GovernedToolCallbackProvider(registry, executor);

        ToolCallback[] callbacks = provider.getToolCallbacks();
        assertEquals(1, callbacks.length);
        assertEquals("lookup_balance", callbacks[0].getToolDefinition().name());
        assertTrue(callbacks[0].getToolDefinition().inputSchema().contains("accountId"));

        Map<String, Object> context = new LinkedHashMap<String, Object>();
        context.put(DefaultSpringAiExecutionContextFactory.PERMISSIONS, Arrays.asList("finance:read"));
        String response = callbacks[0].call("{\"accountId\":7}", new ToolContext(context));

        JsonNode json = objectMapper.readTree(response);
        assertTrue(json.get("success").asBoolean());
        assertEquals("balance-7", json.get("data").asText());
    }

    @Test
    void mapsAdapterContextToSpringAiToolContext() {
        TaskContext task = new TaskContext();
        task.setTaskId("task-1");
        task.setTaskType("refund");
        task.setTaskStatus(TaskStatus.CONFIRMING);
        task.setCurrentStep("confirm_amount");
        ConversationContextHolder.bind(new DefaultConversationSession("session-1", "tenant-a",
                "user-a", "finance", "openai", "gpt-5", Instant.now()), task);

        ExecutionContext executionContext = new ExecutionContext("user-a", "tenant-a", "trace-1",
                Collections.singleton("order:refund"), Instant.now());
        ToolContext toolContext = new SpringAiToolContextAdapter().currentToolContext(executionContext);
        ConversationContextHolder.clear();

        assertEquals("session-1", toolContext.getContext().get(SpringAiToolContextAdapter.SESSION_ID));
        assertEquals("task-1", toolContext.getContext().get(SpringAiToolContextAdapter.TASK_ID));
        assertEquals("CONFIRMING", toolContext.getContext().get(SpringAiToolContextAdapter.TASK_STATUS));
        assertEquals("trace-1", toolContext.getContext().get(DefaultSpringAiExecutionContextFactory.TRACE_ID));
    }

    @Test
    void storesSpringAiMessagesByConversationId() {
        AdapterChatMemoryRepository repository = new AdapterChatMemoryRepository();
        repository.saveAll("session-1", Collections.<Message>singletonList(new UserMessage("hello")));

        List<String> ids = repository.findConversationIds();
        assertEquals(Collections.singletonList("session-1"), ids);
        assertEquals(1, repository.findByConversationId("session-1").size());

        repository.deleteByConversationId("session-1");
        assertTrue(repository.findByConversationId("session-1").isEmpty());
    }

    static class AccountTools {
        @AiTool(name = "lookup_balance", description = "Lookup account balance", paramDescriptions = {"accountId=Account id"})
        public String lookupBalance(Long accountId) {
            return "balance-" + accountId;
        }
    }
}
