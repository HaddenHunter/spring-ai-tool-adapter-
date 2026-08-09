package com.c8software.spring.ai.agent;

import java.time.Instant;

public final class AgentArtifact {
    private final String id;
    private final String runId;
    private final String stepId;
    private final String type;
    private final Object content;
    private final Instant createdAt;

    public AgentArtifact(String id, String runId, String stepId, String type, Object content, Instant createdAt) {
        this.id = id;
        this.runId = runId;
        this.stepId = stepId;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public String getStepId() { return stepId; }
    public String getType() { return type; }
    public Object getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}
