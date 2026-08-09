package com.c8software.spring.ai.core.mcp;

public final class McpProvisionPlan {

    private final String requestId;
    private final McpSkillMatch match;
    private final boolean allowedByPermission;
    private final boolean approvalRequired;
    private final String status;

    public McpProvisionPlan(String requestId, McpSkillMatch match, boolean allowedByPermission,
                            boolean approvalRequired, String status) {
        this.requestId = requestId;
        this.match = match;
        this.allowedByPermission = allowedByPermission;
        this.approvalRequired = approvalRequired;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public McpSkillMatch getMatch() {
        return match;
    }

    public boolean isAllowedByPermission() {
        return allowedByPermission;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public String getStatus() {
        return status;
    }
}
