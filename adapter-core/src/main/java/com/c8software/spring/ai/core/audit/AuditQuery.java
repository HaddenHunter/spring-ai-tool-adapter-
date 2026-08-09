package com.c8software.spring.ai.core.audit;

public final class AuditQuery {

    private final String traceId;
    private final String toolName;
    private final String callerUser;
    private final String tenantId;
    private final String status;
    private final int limit;

    public AuditQuery(String traceId, String toolName, String callerUser, String tenantId, String status, int limit) {
        this.traceId = traceId;
        this.toolName = toolName;
        this.callerUser = callerUser;
        this.tenantId = tenantId;
        this.status = status;
        this.limit = limit <= 0 ? 100 : limit;
    }

    public String getTraceId() { return traceId; }
    public String getToolName() { return toolName; }
    public String getCallerUser() { return callerUser; }
    public String getTenantId() { return tenantId; }
    public String getStatus() { return status; }
    public int getLimit() { return limit; }
}
