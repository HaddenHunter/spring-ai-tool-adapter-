package com.c8software.spring.ai.agent;

public final class ReviewFinding {
    private final String severity;
    private final String message;
    private final boolean blocking;

    public ReviewFinding(String severity, String message, boolean blocking) {
        this.severity = severity;
        this.message = message;
        this.blocking = blocking;
    }

    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public boolean isBlocking() { return blocking; }
}
