package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.enterprise.DefaultEnterpriseAiOperatingSystem;
import com.c8software.spring.ai.core.enterprise.FeedbackSignal;
import com.c8software.spring.ai.core.enterprise.InMemoryLearningFeedbackStore;
import com.c8software.spring.ai.core.enterprise.InMemoryPromptMarketplace;
import com.c8software.spring.ai.core.enterprise.InMemoryTenantRegistry;
import com.c8software.spring.ai.core.enterprise.PromptAsset;
import com.c8software.spring.ai.core.enterprise.RegistryToolMarketplace;
import com.c8software.spring.ai.core.enterprise.TenantProfile;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
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
        assertThat(status.get("toolCount")).isEqualTo(1);
        assertThat(status.get("feedbackCount")).isEqualTo(1);
    }

    static class EnterpriseTools {
        @AiTool(name = "enterprise_ping", description = "ping")
        public String ping() {
            return "pong";
        }
    }
}
