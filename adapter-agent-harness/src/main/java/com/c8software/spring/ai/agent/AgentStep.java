package com.c8software.spring.ai.agent;

public final class AgentStep {
    private final String id;
    private final String name;
    private final AgentStepType type;
    private final String toolName;
    private final String argumentsJson;
    private final int maxRepairAttempts;

    public AgentStep(String id, String name, AgentStepType type, String toolName,
                     String argumentsJson, int maxRepairAttempts) {
        this.id = id;
        this.name = name;
        this.type = type == null ? AgentStepType.NOOP : type;
        this.toolName = toolName;
        this.argumentsJson = argumentsJson == null ? "{}" : argumentsJson;
        this.maxRepairAttempts = Math.max(0, maxRepairAttempts);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public AgentStepType getType() { return type; }
    public String getToolName() { return toolName; }
    public String getArgumentsJson() { return argumentsJson; }
    public int getMaxRepairAttempts() { return maxRepairAttempts; }
}
