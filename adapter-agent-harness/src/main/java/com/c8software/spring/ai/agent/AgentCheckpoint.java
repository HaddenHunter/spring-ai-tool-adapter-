package com.c8software.spring.ai.agent;

import java.time.Instant;

public final class AgentCheckpoint {
    private final String id;
    private final String runId;
    private final String phaseId;
    private final String stepId;
    private final AgentRunStatus status;
    private final Instant createdAt;

    public AgentCheckpoint(String id, String runId, String phaseId, String stepId,
                           AgentRunStatus status, Instant createdAt) {
        this.id = id;
        this.runId = runId;
        this.phaseId = phaseId;
        this.stepId = stepId;
        this.status = status;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public String getPhaseId() { return phaseId; }
    public String getStepId() { return stepId; }
    public AgentRunStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
