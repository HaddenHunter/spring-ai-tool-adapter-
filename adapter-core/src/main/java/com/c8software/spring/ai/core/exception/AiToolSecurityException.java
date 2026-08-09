package com.c8software.spring.ai.core.exception;

/** Security and masking related tool exception. */
public class AiToolSecurityException extends AiToolException {
    private static final long serialVersionUID = 1L;

    public AiToolSecurityException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AiToolSecurityException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
