package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.execution.ExecutionContext;

public final class AgentStepRequest {
    private final AgentFlowDefinition flow;
    private final AgentPhase phase;
    private final AgentStep step;
    private final AgentRunState runState;
    private final ExecutionContext executionContext;

    public AgentStepRequest(AgentFlowDefinition flow, AgentPhase phase, AgentStep step,
                            AgentRunState runState, ExecutionContext executionContext) {
        this.flow = flow;
        this.phase = phase;
        this.step = step;
        this.runState = runState;
        this.executionContext = executionContext;
    }

    public AgentFlowDefinition getFlow() { return flow; }
    public AgentPhase getPhase() { return phase; }
    public AgentStep getStep() { return step; }
    public AgentRunState getRunState() { return runState; }
    public ExecutionContext getExecutionContext() { return executionContext; }
}
