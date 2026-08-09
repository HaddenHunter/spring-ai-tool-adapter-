package com.c8software.spring.ai.core.config;

import com.c8software.spring.ai.core.audit.AsyncAuditLogger;
import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.JdbcAuditLogger;
import com.c8software.spring.ai.core.approval.DefaultToolApprovalManager;
import com.c8software.spring.ai.core.approval.ManualApprovalRequiredHumanInTheLoop;
import com.c8software.spring.ai.core.approval.ToolApprovalManager;
import com.c8software.spring.ai.core.context.ContextCompressor;
import com.c8software.spring.ai.core.context.ConversationSessionStore;
import com.c8software.spring.ai.core.context.DefaultContextCompressor;
import com.c8software.spring.ai.core.context.DefaultUserChoiceTracker;
import com.c8software.spring.ai.core.context.InMemoryConversationSessionStore;
import com.c8software.spring.ai.core.context.UserChoiceTracker;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.execution.TimeoutToolInvocationExecutor;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.execution.ToolInvocationExecutor;
import com.c8software.spring.ai.core.enterprise.DefaultEnterpriseAiOperatingSystem;
import com.c8software.spring.ai.core.enterprise.EnterpriseAiOperatingSystem;
import com.c8software.spring.ai.core.enterprise.InMemoryLearningFeedbackStore;
import com.c8software.spring.ai.core.enterprise.InMemoryPromptMarketplace;
import com.c8software.spring.ai.core.enterprise.InMemoryTenantRegistry;
import com.c8software.spring.ai.core.enterprise.LearningFeedbackStore;
import com.c8software.spring.ai.core.enterprise.PromptMarketplace;
import com.c8software.spring.ai.core.enterprise.RegistryToolMarketplace;
import com.c8software.spring.ai.core.enterprise.TenantRegistry;
import com.c8software.spring.ai.core.enterprise.ToolMarketplace;
import com.c8software.spring.ai.core.hub.BusinessAiHub;
import com.c8software.spring.ai.core.hub.ConversationReplayStore;
import com.c8software.spring.ai.core.hub.DefaultBusinessAiHub;
import com.c8software.spring.ai.core.hub.InMemoryConversationReplayStore;
import com.c8software.spring.ai.core.idempotency.DefaultIdempotencyKeyResolver;
import com.c8software.spring.ai.core.idempotency.IdempotencyKeyResolver;
import com.c8software.spring.ai.core.idempotency.IdempotencyStore;
import com.c8software.spring.ai.core.idempotency.InMemoryIdempotencyStore;
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
import com.c8software.spring.ai.core.security.DefaultResultMasker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.c8software.spring.ai.core.security.PermissionChecker;
import com.c8software.spring.ai.core.security.ResultMasker;
import com.c8software.spring.ai.core.security.SensitiveMasker;
import com.c8software.spring.ai.core.orchestration.HumanInTheLoop;
import com.c8software.spring.ai.core.visibility.DefaultToolVisibilityFilter;
import com.c8software.spring.ai.core.visibility.ToolVisibilityFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/** Default Spring Boot configuration for the adapter core. */
@AutoConfiguration
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
    public ResultMasker resultMasker(SensitiveMasker sensitiveMasker) {
        return new DefaultResultMasker(sensitiveMasker);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    public AuditLogger jdbcAuditLogger(DataSource dataSource) {
        JdbcAuditLogger logger = new JdbcAuditLogger(new JdbcTemplate(dataSource));
        logger.initializeSchema();
        return logger;
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
    public ConversationReplayStore conversationReplayStore() {
        return new InMemoryConversationReplayStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public HumanInTheLoop humanInTheLoop() {
        return new ManualApprovalRequiredHumanInTheLoop();
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolApprovalManager toolApprovalManager(HumanInTheLoop humanInTheLoop) {
        return new DefaultToolApprovalManager(humanInTheLoop);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyStore idempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyKeyResolver idempotencyKeyResolver() {
        return new DefaultIdempotencyKeyResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolVisibilityFilter toolVisibilityFilter() {
        return new DefaultToolVisibilityFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantRegistry tenantRegistry() {
        return new InMemoryTenantRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public PromptMarketplace promptMarketplace() {
        return new InMemoryPromptMarketplace();
    }

    @Bean
    @ConditionalOnMissingBean
    public LearningFeedbackStore learningFeedbackStore() {
        return new InMemoryLearningFeedbackStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolMarketplace toolMarketplace(ToolRegistry registry) {
        return new RegistryToolMarketplace(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public EnterpriseAiOperatingSystem enterpriseAiOperatingSystem(TenantRegistry tenantRegistry,
                                                                   PromptMarketplace promptMarketplace,
                                                                   ToolMarketplace toolMarketplace,
                                                                   LearningFeedbackStore feedbackStore) {
        return new DefaultEnterpriseAiOperatingSystem(tenantRegistry, promptMarketplace, toolMarketplace, feedbackStore);
    }

    @Bean
    @ConditionalOnMissingBean
    public ToolInvocationExecutor toolInvocationExecutor() {
        return new TimeoutToolInvocationExecutor();
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
                                     ObjectMapper objectMapper, AiToolProperties properties,
                                     ToolApprovalManager approvalManager, IdempotencyStore idempotencyStore,
                                     IdempotencyKeyResolver idempotencyKeyResolver,
                                     ToolInvocationExecutor invocationExecutor, ResultMasker resultMasker) {
        return new DefaultToolExecutor(registry, permissionChecker, sensitiveMasker, auditLogger, objectMapper,
                properties, approvalManager, idempotencyStore, idempotencyKeyResolver, invocationExecutor, resultMasker);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusinessAiHub businessAiHub(ConversationSessionStore sessionStore,
                                       ConversationReplayStore replayStore,
                                       ToolExecutor toolExecutor) {
        return new DefaultBusinessAiHub(sessionStore, replayStore, toolExecutor);
    }
}
