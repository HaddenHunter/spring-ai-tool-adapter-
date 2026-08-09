package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.execution.ExecutionContext;

public interface AgentHarness {
    AgentRunState start(AgentFlowDefinition flow, ExecutionContext executionContext);

    AgentRunState resume(String runId, AgentFlowDefinition flow, ExecutionContext executionContext);
}
