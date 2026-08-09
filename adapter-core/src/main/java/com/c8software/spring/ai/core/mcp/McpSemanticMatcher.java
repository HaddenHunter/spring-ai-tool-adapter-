package com.c8software.spring.ai.core.mcp;

import java.util.List;

public interface McpSemanticMatcher {

    List<McpSkillMatch> match(McpSemanticRequest request, List<McpCapabilityDescriptor> candidates);
}
