package com.c8software.spring.ai.core.context;

import java.time.Instant;

public final class ContextFact {

    private final String name;
    private final Object value;
    private final boolean confirmed;
    private final String source;
    private final Instant timestamp;

    public ContextFact(String name, Object value, boolean confirmed, String source, Instant timestamp) {
        this.name = name;
        this.value = value;
        this.confirmed = confirmed;
        this.source = source;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getSource() {
        return source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
