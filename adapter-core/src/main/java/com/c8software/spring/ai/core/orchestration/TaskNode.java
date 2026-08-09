package com.c8software.spring.ai.core.orchestration;

public class TaskNode {

    private String id;

    private TaskNodeType type;

    private String toolName;

    private String argumentsJson;

    public TaskNode() {
    }

    public TaskNode(String id, TaskNodeType type, String toolName, String argumentsJson) {
        this.id = id;
        this.type = type;
        this.toolName = toolName;
        this.argumentsJson = argumentsJson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TaskNodeType getType() {
        return type;
    }

    public void setType(TaskNodeType type) {
        this.type = type;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getArgumentsJson() {
        return argumentsJson;
    }

    public void setArgumentsJson(String argumentsJson) {
        this.argumentsJson = argumentsJson;
    }
}
