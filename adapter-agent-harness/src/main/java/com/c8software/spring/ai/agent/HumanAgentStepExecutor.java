package com.c8software.spring.ai.agent;

public class HumanAgentStepExecutor implements AgentStepExecutor {
    public boolean supports(AgentStep step) {
        return step != null && AgentStepType.HUMAN.equals(step.getType());
    }

    public AgentStepResult execute(AgentStepRequest request) {
        return AgentStepResult.waiting("Waiting for human action at step: " + request.getStep().getId());
    }
}
