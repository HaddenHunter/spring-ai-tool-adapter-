package com.c8software.spring.ai.core.mcp;

public interface McpProvisioningPlanner {

    McpProvisionPlan plan(McpSemanticRequest request);
}
