package com.c8software.spring.ai.core.hub;

import com.c8software.spring.ai.core.context.ContextSnapshot;
import com.c8software.spring.ai.core.execution.ToolResult;

/** Response returned by the business AI hub. */
public final class BusinessAiHubResponse {
    private final String sessionId;
    private final String taskId;
    private final String taskStatus;
    private final ToolResult toolResult;
    private final ContextSnapshot contextSnapshot;

    public BusinessAiHubResponse(String sessionId, String taskId, String taskStatus,
                                 ToolResult toolResult, ContextSnapshot contextSnapshot) {
        this.sessionId = sessionId;
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.toolResult = toolResult;
        this.contextSnapshot = contextSnapshot;
    }

    public String getSessionId() { return sessionId; }
    public String getTaskId() { return taskId; }
    public String getTaskStatus() { return taskStatus; }
    public ToolResult getToolResult() { return toolResult; }
    public ContextSnapshot getContextSnapshot() { return contextSnapshot; }
}
