package com.c8software.spring.ai.core.mcp;

public final class McpSkillMatch {

    private final McpCapabilityDescriptor capability;
    private final double score;
    private final String reason;

    public McpSkillMatch(McpCapabilityDescriptor capability, double score, String reason) {
        this.capability = capability;
        this.score = score;
        this.reason = reason;
    }

    public McpCapabilityDescriptor getCapability() {
        return capability;
    }

    public double getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }
}
