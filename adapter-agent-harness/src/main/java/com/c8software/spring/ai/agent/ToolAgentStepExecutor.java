package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.execution.ToolResult;

public class ToolAgentStepExecutor implements AgentStepExecutor {
    private final ToolExecutor toolExecutor;

    public ToolAgentStepExecutor(ToolExecutor toolExecutor) {
        this.toolExecutor = toolExecutor;
    }

    public boolean supports(AgentStep step) {
        return step != null && AgentStepType.TOOL.equals(step.getType());
    }

    public AgentStepResult execute(AgentStepRequest request) {
        ToolResult result = toolExecutor.execute(request.getStep().getToolName(),
                request.getStep().getArgumentsJson(), request.getExecutionContext());
        return result.isSuccess()
                ? AgentStepResult.success(result.getData())
                : AgentStepResult.failure(result.getErrorMessage());
    }
}
