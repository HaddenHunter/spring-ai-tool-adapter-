package com.c8software.spring.ai.core.mcp;

import java.util.List;
import java.util.UUID;

public class DefaultMcpProvisioningPlanner implements McpProvisioningPlanner {

    private final McpCapabilityCatalog catalog;
    private final McpSemanticMatcher matcher;

    public DefaultMcpProvisioningPlanner(McpCapabilityCatalog catalog, McpSemanticMatcher matcher) {
        this.catalog = catalog;
        this.matcher = matcher;
    }

    public McpProvisionPlan plan(McpSemanticRequest request) {
        List<McpSkillMatch> matches = matcher.match(request, catalog.list());
        if (matches.isEmpty()) {
            return new McpProvisionPlan(UUID.randomUUID().toString(), null, false, false, "NO_MATCH");
        }
        McpSkillMatch best = matches.get(0);
        boolean allowed = hasPermissions(request, best.getCapability());
        boolean approvalRequired = best.getCapability().isApprovalRequired()
                || best.getCapability().getRiskLevel() == McpRiskLevel.HIGH
                || best.getCapability().getRiskLevel() == McpRiskLevel.CRITICAL;
        String status = allowed ? "PENDING_APPROVAL" : "PERMISSION_REQUIRED";
        if (allowed && !approvalRequired) {
            status = "READY_TO_PROVISION";
        }
        return new McpProvisionPlan(UUID.randomUUID().toString(), best, allowed, approvalRequired, status);
    }

    private boolean hasPermissions(McpSemanticRequest request, McpCapabilityDescriptor capability) {
        if (capability.getRequiredPermissions().isEmpty()) {
            return true;
        }
        if (request == null) {
            return false;
        }
        return request.getPermissions().containsAll(capability.getRequiredPermissions());
    }
}
