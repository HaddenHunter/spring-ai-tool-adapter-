package com.c8software.spring.ai.core.exception;

/** Base serializable exception for the AI tool adapter. */
public class AiToolException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String errorCode;

    public AiToolException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiToolException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /** Returns stable error code. */
    public String getErrorCode() {
        return errorCode;
    }
}
