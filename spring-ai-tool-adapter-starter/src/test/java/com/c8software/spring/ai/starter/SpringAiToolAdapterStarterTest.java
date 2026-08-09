package com.c8software.spring.ai.starter;

import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.context.ConversationSessionStore;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SpringAiToolAdapterStarterTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    com.c8software.spring.ai.core.config.AiToolAutoConfiguration.class));

    @Test
    void autoConfigurationCreatesCoreBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ToolRegistry.class);
            assertThat(context).hasSingleBean(ToolExecutor.class);
            assertThat(context).hasSingleBean(AuditLogger.class);
            assertThat(context).hasSingleBean(ConversationSessionStore.class);
        });
    }
}
