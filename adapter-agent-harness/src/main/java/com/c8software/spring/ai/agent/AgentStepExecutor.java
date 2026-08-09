package com.c8software.spring.ai.agent;

public interface AgentStepExecutor {
    boolean supports(AgentStep step);

    AgentStepResult execute(AgentStepRequest request);
}
