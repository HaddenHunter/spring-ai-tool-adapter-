package com.c8software.spring.ai.core.hub;

import com.c8software.spring.ai.core.execution.ExecutionContext;

/** Request handled by the business AI hub. */
public final class BusinessAiHubRequest {
    private final String sessionId;
    private final String taskId;
    private final String taskType;
    private final String userInput;
    private final String toolName;
    private final String argumentsJson;
    private final ExecutionContext executionContext;

    public BusinessAiHubRequest(String sessionId, String taskId, String taskType, String userInput,
                                String toolName, String argumentsJson, ExecutionContext executionContext) {
        this.sessionId = sessionId;
        this.taskId = taskId;
        this.taskType = taskType;
        this.userInput = userInput;
        this.toolName = toolName;
        this.argumentsJson = argumentsJson;
        this.executionContext = executionContext;
    }

    public String getSessionId() { return sessionId; }
    public String getTaskId() { return taskId; }
    public String getTaskType() { return taskType; }
    public String getUserInput() { return userInput; }
    public String getToolName() { return toolName; }
    public String getArgumentsJson() { return argumentsJson; }
    public ExecutionContext getExecutionContext() { return executionContext; }
}
