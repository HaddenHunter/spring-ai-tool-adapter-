package com.c8software.spring.ai.core.config;

import com.c8software.spring.ai.core.audit.AsyncAuditLogger;
import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.context.ContextCompressor;
import com.c8software.spring.ai.core.context.ConversationSessionStore;
import com.c8software.spring.ai.core.context.DefaultContextCompressor;
import com.c8software.spring.ai.core.context.DefaultUserChoiceTracker;
import com.c8software.spring.ai.core.context.InMemoryConversationSessionStore;
import com.c8software.spring.ai.core.context.UserChoiceTracker;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.mcp.DefaultMcpProvisioningPlanner;
import com.c8software.spring.ai.core.mcp.DefaultMcpSemanticMatcher;
import com.c8software.spring.ai.core.mcp.InMemoryMcpCapabilityCatalog;
import com.c8software.spring.ai.core.mcp.McpCapabilityCatalog;
import com.c8software.spring.ai.core.mcp.McpProvisioningPlanner;
import com.c8software.spring.ai.core.mcp.McpSemanticMatcher;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.DefaultToolGovernanceAnnotationProcessor;
import com.c8software.spring.ai.core.registry.ToolGovernanceAnnotationProcessor;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.DefaultPermissionChecker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.c8software.spring.ai.core.security.PermissionChecker;
import com.c8software.spring.ai.core.security.SensitiveMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Default Spring Boot configuration for the adapter core. */
@Configuration
@EnableConfigurationProperties(AiToolProperties.class)
public class AiToolAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ToolRegistry toolRegistry() {
        return new ToolRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolGovernanceAnnotationProcessor toolGovernanceAnnotationProcessor() {
        return new DefaultToolGovernanceAnnotationProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public AiToolRegistrar aiToolRegistrar(ToolRegistry registry, AiToolProperties properties,
                                           ToolGovernanceAnnotationProcessor governanceAnnotationProcessor) {
        return new AiToolRegistrar(registry, properties, governanceAnnotationProcessor);
    }

    @Bean
    @ConditionalOnMissingBean
    public PermissionChecker permissionChecker() {
        return new DefaultPermissionChecker();
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitiveMasker sensitiveMasker() {
        return new DefaultSensitiveMasker();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditLogger auditLogger() {
        return new AsyncAuditLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConversationSessionStore conversationSessionStore() {
        return new InMemoryConversationSessionStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserChoiceTracker userChoiceTracker() {
        return new DefaultUserChoiceTracker();
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextCompressor contextCompressor() {
        return new DefaultContextCompressor();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpCapabilityCatalog mcpCapabilityCatalog() {
        return new InMemoryMcpCapabilityCatalog();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpSemanticMatcher mcpSemanticMatcher() {
        return new DefaultMcpSemanticMatcher();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpProvisioningPlanner mcpProvisioningPlanner(McpCapabilityCatalog catalog, McpSemanticMatcher matcher) {
        return new DefaultMcpProvisioningPlanner(catalog, matcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolExecutor toolExecutor(ToolRegistry registry, PermissionChecker permissionChecker,
                                     SensitiveMasker sensitiveMasker, AuditLogger auditLogger,
                                     ObjectMapper objectMapper, AiToolProperties properties) {
        return new DefaultToolExecutor(registry, permissionChecker, sensitiveMasker, auditLogger, objectMapper, properties);
    }
}
