package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.execution.ToolExecutor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
public class AgentHarnessAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AgentRunStore agentRunStore() {
        return new InMemoryAgentRunStore();
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
                                     AgentRepairLoop repairLoop) {
        return new DefaultAgentHarness(new ArrayList<AgentStepExecutor>(stepExecutors), runStore, repairLoop);
    }
}
