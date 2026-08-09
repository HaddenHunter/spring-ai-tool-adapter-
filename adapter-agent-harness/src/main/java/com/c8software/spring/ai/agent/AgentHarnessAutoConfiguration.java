package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.config.AiToolAutoConfiguration;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@AutoConfigureAfter(AiToolAutoConfiguration.class)
public class AgentHarnessAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper agentHarnessObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    public AgentRunStore jdbcAgentRunStore(DataSource dataSource, ObjectMapper objectMapper) {
        JdbcAgentRunStore store = new JdbcAgentRunStore(new JdbcTemplate(dataSource), objectMapper);
        store.initializeSchema();
        return store;
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentRunStore agentRunStore() {
        return new InMemoryAgentRunStore();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    public ArtifactStore jdbcArtifactStore(DataSource dataSource, ObjectMapper objectMapper) {
        JdbcArtifactStore store = new JdbcArtifactStore(new JdbcTemplate(dataSource), objectMapper);
        store.initializeSchema();
        return store;
    }

    @Bean
    @ConditionalOnMissingBean
    public ArtifactStore artifactStore() {
        return new InMemoryArtifactStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReviewGate reviewGate() {
        return new PassReviewGate();
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentRepairLoop agentRepairLoop() {
        return new NoopAgentRepairLoop();
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentFlowSpecParser agentFlowSpecParser() {
        return new JacksonAgentFlowSpecParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReviewAgentStepExecutor reviewAgentStepExecutor(ReviewGate reviewGate) {
        return new ReviewAgentStepExecutor(reviewGate);
    }

    @Bean
    @ConditionalOnMissingBean
    public HumanAgentStepExecutor humanAgentStepExecutor() {
        return new HumanAgentStepExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public NoopAgentStepExecutor noopAgentStepExecutor() {
        return new NoopAgentStepExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ToolExecutor.class)
    public ToolAgentStepExecutor toolAgentStepExecutor(ToolExecutor toolExecutor) {
        return new ToolAgentStepExecutor(toolExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public AgentHarness agentHarness(List<AgentStepExecutor> stepExecutors,
                                     AgentRunStore runStore,
                                     ArtifactStore artifactStore,
                                     AgentRepairLoop repairLoop) {
        return new DefaultAgentHarness(new ArrayList<AgentStepExecutor>(stepExecutors), runStore, artifactStore, repairLoop);
    }
}
