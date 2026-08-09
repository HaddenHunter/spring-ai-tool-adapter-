package com.c8software.spring.ai.core.exception;

/** Registration-time tool exception. */
public class AiToolRegistrationException extends AiToolException {
    private static final long serialVersionUID = 1L;

    public AiToolRegistrationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AiToolRegistrationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
