package com.c8software.spring.ai.core.execution;

/** Tool execution result wrapper. */
public final class ToolResult {
    private final boolean success;
    private final Object data;
    private final String errorCode;
    private final String errorMessage;
    private final long costMs;

    private ToolResult(boolean success, Object data, String errorCode, String errorMessage, long costMs) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.costMs = costMs;
    }

    public static ToolResult success(Object data, long costMs) {
        return new ToolResult(true, data, null, null, costMs);
    }

    public static ToolResult failure(String errorCode, String errorMessage, long costMs) {
        return new ToolResult(false, null, errorCode, errorMessage, costMs);
    }

    public boolean isSuccess() { return success; }
    public Object getData() { return data; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public long getCostMs() { return costMs; }
}
