package com.c8software.spring.ai.agent;

public class NoopAgentStepExecutor implements AgentStepExecutor {
    public boolean supports(AgentStep step) {
        return step != null && AgentStepType.NOOP.equals(step.getType());
    }

    public AgentStepResult execute(AgentStepRequest request) {
        return AgentStepResult.success("noop");
    }
}
