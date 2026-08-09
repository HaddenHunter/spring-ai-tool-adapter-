package com.c8software.spring.ai.springai;

import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Auto-configuration for Spring AI ToolCallback integration. */
@Configuration
@ConditionalOnClass(ToolCallbackProvider.class)
public class SpringAiToolAdapterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SpringAiExecutionContextFactory springAiExecutionContextFactory() {
        return new DefaultSpringAiExecutionContextFactory();
    }

    @Bean
    @ConditionalOnBean({ToolRegistry.class, ToolExecutor.class})
    @ConditionalOnMissingBean(ToolCallbackProvider.class)
    public ToolCallbackProvider governedToolCallbackProvider(ToolRegistry toolRegistry,
                                                             ToolExecutor toolExecutor,
                                                             SpringAiExecutionContextFactory contextFactory,
                                                             ObjectMapper objectMapper) {
        return new GovernedToolCallbackProvider(toolRegistry, toolExecutor, contextFactory, objectMapper);
    }
}
