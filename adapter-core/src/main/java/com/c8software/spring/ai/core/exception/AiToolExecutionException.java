package com.c8software.spring.ai.core.exception;

/** Runtime tool execution exception. */
public class AiToolExecutionException extends AiToolException {
    private static final long serialVersionUID = 1L;

    public AiToolExecutionException(String errorCode, String message) {
        super(errorCode, message);
    }

    public AiToolExecutionException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
