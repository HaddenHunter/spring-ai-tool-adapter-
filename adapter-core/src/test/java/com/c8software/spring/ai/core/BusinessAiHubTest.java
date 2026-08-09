package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.audit.AsyncAuditLogger;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.context.InMemoryConversationSessionStore;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.hub.BusinessAiHubResponse;
import com.c8software.spring.ai.core.hub.BusinessAiHubRequest;
import com.c8software.spring.ai.core.hub.ConversationTurn;
import com.c8software.spring.ai.core.hub.DefaultBusinessAiHub;
import com.c8software.spring.ai.core.hub.InMemoryConversationReplayStore;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.DefaultPermissionChecker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessAiHubTest {

    @Test
    void handlesToolCallWithSessionContextAndReplayTurn() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new HubTools(), "hubTools");
        InMemoryConversationSessionStore sessionStore = new InMemoryConversationSessionStore();
        InMemoryConversationReplayStore replayStore = new InMemoryConversationReplayStore();
        DefaultToolExecutor executor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                new AsyncAuditLogger(),
                new ObjectMapper()
        );
        DefaultBusinessAiHub hub = new DefaultBusinessAiHub(sessionStore, replayStore, executor);

        BusinessAiHubResponse response = hub.handle(new BusinessAiHubRequest(
                "session-1",
                "task-1",
                "greeting",
                "say hello to Ada",
                "hub_hello",
                "{\"name\":\"Ada\"}",
                new ExecutionContext("user-1", "tenant-1", "trace-1", Collections.emptySet(), Instant.now())
        ));

        assertThat(response.getSessionId()).isEqualTo("session-1");
        assertThat(response.getTaskStatus()).isEqualTo("DONE");
        assertThat(response.getToolResult().getData()).isEqualTo("Hello Ada");
        assertThat(response.getContextSnapshot().getTaskId()).isEqualTo("task-1");

        List<ConversationTurn> turns = replayStore.list("tenant-1", "session-1", 10);
        assertThat(turns).hasSize(1);
        assertThat(turns.get(0).getUserInput()).isEqualTo("say hello to Ada");
        assertThat(turns.get(0).getBeforeSnapshot().getTaskStatus().name()).isEqualTo("COLLECTING");
        assertThat(turns.get(0).getAfterSnapshot().getTaskStatus().name()).isEqualTo("DONE");
    }

    static class HubTools {
        @AiTool(name = "hub_hello", description = "hello", paramDescriptions = "name")
        public String hello(String name) {
            return "Hello " + name;
        }
    }
}
