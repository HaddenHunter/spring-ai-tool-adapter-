package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolVisibility;
import com.c8software.spring.ai.core.annotation.ToolVisibility;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.enterprise.DefaultEnterpriseAiOperatingSystem;
import com.c8software.spring.ai.core.enterprise.FeedbackSignal;
import com.c8software.spring.ai.core.enterprise.InMemoryLearningFeedbackStore;
import com.c8software.spring.ai.core.enterprise.InMemoryPromptMarketplace;
import com.c8software.spring.ai.core.enterprise.InMemoryTenantRegistry;
import com.c8software.spring.ai.core.enterprise.PromptAsset;
import com.c8software.spring.ai.core.enterprise.RegistryToolMarketplace;
import com.c8software.spring.ai.core.enterprise.TenantProfile;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.hub.ConversationTurn;
import com.c8software.spring.ai.core.hub.InMemoryConversationReplayStore;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.visibility.DefaultToolVisibilityFilter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnterpriseAiOperatingSystemTest {

    @Test
    void reportsMarketplaceTenantAndFeedbackCounts() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new EnterpriseTools(), "enterpriseTools");
        InMemoryTenantRegistry tenantRegistry = new InMemoryTenantRegistry();
        InMemoryPromptMarketplace promptMarketplace = new InMemoryPromptMarketplace();
        InMemoryLearningFeedbackStore feedbackStore = new InMemoryLearningFeedbackStore();

        tenantRegistry.save(new TenantProfile("tenant-1", "Tenant One", "private",
                Collections.singleton("enterprise"), Instant.now()));
        promptMarketplace.publish(new PromptAsset("prompt-1", "Support", "1.0.0", "ops",
                "APPROVED", "Be helpful.", Instant.now()));
        feedbackStore.record(new FeedbackSignal(UUID.randomUUID().toString(), "tenant-1",
                "tool", "enterprise_ping", 5, "works", Instant.now()));

        DefaultEnterpriseAiOperatingSystem os = new DefaultEnterpriseAiOperatingSystem(
                tenantRegistry,
                promptMarketplace,
                new RegistryToolMarketplace(registry),
                feedbackStore
        );

        Map<String, Object> status = os.status();

        assertThat(status.get("status")).isEqualTo("BASELINE");
        assertThat(status.get("tenantCount")).isEqualTo(1);
        assertThat(status.get("promptCount")).isEqualTo(1);
        assertThat(status.get("toolCount")).isEqualTo(2);
        assertThat(status.get("feedbackCount")).isEqualTo(1);
    }

    @Test
    void isolatesReplayAndFeedbackByTenant() {
        InMemoryConversationReplayStore replayStore = new InMemoryConversationReplayStore();
        replayStore.append(turn("tenant-a", "session-1", "query A"));
        replayStore.append(turn("tenant-b", "session-1", "query B"));

        assertThat(replayStore.list("tenant-a", "session-1", 10))
                .extracting(ConversationTurn::getUserInput)
                .containsExactly("query A");
        assertThat(replayStore.list("tenant-b", "session-1", 10))
                .extracting(ConversationTurn::getUserInput)
                .containsExactly("query B");

        InMemoryLearningFeedbackStore feedbackStore = new InMemoryLearningFeedbackStore();
        feedbackStore.record(new FeedbackSignal("signal-a", "tenant-a", "tool", "enterprise_ping", 5, "a", Instant.now()));
        feedbackStore.record(new FeedbackSignal("signal-b", "tenant-b", "tool", "enterprise_ping", 1, "b", Instant.now()));

        assertThat(feedbackStore.list("tenant-a", 10))
                .extracting(FeedbackSignal::getId)
                .containsExactly("signal-a");
        assertThat(feedbackStore.list("tenant-b", 10))
                .extracting(FeedbackSignal::getId)
                .containsExactly("signal-b");
    }

    @Test
    void keepsInternalToolsOutOfTenantPublicScope() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new EnterpriseTools(), "enterpriseTools");

        DefaultToolVisibilityFilter filter = new DefaultToolVisibilityFilter();
        ExecutionContext tenantPublic = new ExecutionContext("user-a", "tenant-a", "trace-a",
                Collections.<String>emptySet(), Instant.now());
        ExecutionContext tenantAdmin = new ExecutionContext("user-b", "tenant-b", "trace-b",
                new LinkedHashSet<String>(Arrays.asList("tool:internal")), Instant.now());

        assertThat(filter.filter(registry.listAll(), tenantPublic))
                .extracting(tool -> tool.getName())
                .doesNotContain("enterprise_internal_ping");
        assertThat(filter.filter(registry.listAll(), tenantAdmin))
                .extracting(tool -> tool.getName())
                .contains("enterprise_internal_ping");
    }

    private ConversationTurn turn(String tenantId, String sessionId, String input) {
        return new ConversationTurn(UUID.randomUUID().toString(), sessionId, tenantId, "user",
                input, "enterprise_ping", "SUCCESS", null, null, Collections.<String, Object>emptyMap(), Instant.now());
    }

    static class EnterpriseTools {
        @AiTool(name = "enterprise_ping", description = "ping")
        public String ping() {
            return "pong";
        }

        @AiTool(name = "enterprise_internal_ping", description = "internal ping")
        @AiToolVisibility(ToolVisibility.INTERNAL)
        public String internalPing() {
            return "pong";
        }
    }
}
