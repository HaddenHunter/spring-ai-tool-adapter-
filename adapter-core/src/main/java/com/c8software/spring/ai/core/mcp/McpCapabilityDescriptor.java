package com.c8software.spring.ai.core.mcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class McpCapabilityDescriptor {

    private final String id;
    private final String name;
    private final String description;
    private final List<String> semanticTags;
    private final List<String> requiredPermissions;
    private final McpRiskLevel riskLevel;
    private final boolean approvalRequired;

    public McpCapabilityDescriptor(String id, String name, String description, List<String> semanticTags,
                                   List<String> requiredPermissions, McpRiskLevel riskLevel,
                                   boolean approvalRequired) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.semanticTags = Collections.unmodifiableList(new ArrayList<String>(semanticTags == null
                ? Collections.<String>emptyList()
                : semanticTags));
        this.requiredPermissions = Collections.unmodifiableList(new ArrayList<String>(requiredPermissions == null
                ? Collections.<String>emptyList()
                : requiredPermissions));
        this.riskLevel = riskLevel == null ? McpRiskLevel.MEDIUM : riskLevel;
        this.approvalRequired = approvalRequired;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSemanticTags() {
        return semanticTags;
    }

    public List<String> getRequiredPermissions() {
        return requiredPermissions;
    }

    public McpRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }
}
