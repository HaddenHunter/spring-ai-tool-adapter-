package com.c8software.spring.ai.core.audit;

import java.time.Instant;

/** Serializable audit event for one tool call. */
public final class AuditRecord {
    private final String traceId;
    private final String toolName;
    private final String callerUser;
    private final String tenantId;
    private final String inputHash;
    private final String outputHash;
    private final long costMs;
    private final String status;
    private final String errorMessage;
    private final String eventType;
    private final String contextBeforeHash;
    private final String contextAfterHash;
    private final Instant timestamp;

    public AuditRecord(String traceId, String toolName, String callerUser, String tenantId,
                       String inputHash, String outputHash, long costMs, String status,
                       String errorMessage, Instant timestamp) {
        this(traceId, toolName, callerUser, tenantId, inputHash, outputHash, costMs, status,
                errorMessage, "TOOL_CALL", null, null, timestamp);
    }

    public AuditRecord(String traceId, String toolName, String callerUser, String tenantId,
                       String inputHash, String outputHash, long costMs, String status,
                       String errorMessage, String eventType, String contextBeforeHash,
                       String contextAfterHash, Instant timestamp) {
        this.traceId = traceId;
        this.toolName = toolName;
        this.callerUser = callerUser;
        this.tenantId = tenantId;
        this.inputHash = inputHash;
        this.outputHash = outputHash;
        this.costMs = costMs;
        this.status = status;
        this.errorMessage = errorMessage;
        this.eventType = eventType == null ? "TOOL_CALL" : eventType;
        this.contextBeforeHash = contextBeforeHash;
        this.contextAfterHash = contextAfterHash;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public String getTraceId() { return traceId; }
    public String getToolName() { return toolName; }
    public String getCallerUser() { return callerUser; }
    public String getTenantId() { return tenantId; }
    public String getInputHash() { return inputHash; }
    public String getOutputHash() { return outputHash; }
    public long getCostMs() { return costMs; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public String getEventType() { return eventType; }
    public String getContextBeforeHash() { return contextBeforeHash; }
    public String getContextAfterHash() { return contextAfterHash; }
    public Instant getTimestamp() { return timestamp; }
}
