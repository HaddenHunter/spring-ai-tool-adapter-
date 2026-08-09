package com.c8software.spring.ai.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AgentRunState {
    private final String runId;
    private final String flowId;
    private AgentRunStatus status = AgentRunStatus.CREATED;
    private String currentPhaseId;
    private String currentStepId;
    private String errorMessage;
    private final Set<String> completedStepIds = new LinkedHashSet<String>();
    private final List<AgentCheckpoint> checkpoints = new ArrayList<AgentCheckpoint>();
    private final List<AgentArtifact> artifacts = new ArrayList<AgentArtifact>();
    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private final Instant createdAt;
    private Instant updatedAt;

    public AgentRunState(String runId, String flowId, Instant createdAt) {
        this.runId = runId;
        this.flowId = flowId;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = this.createdAt;
    }

    public String getRunId() { return runId; }
    public String getFlowId() { return flowId; }
    public AgentRunStatus getStatus() { return status; }
    public void setStatus(AgentRunStatus status) { this.status = status; touch(); }
    public String getCurrentPhaseId() { return currentPhaseId; }
    public void setCurrentPhaseId(String currentPhaseId) { this.currentPhaseId = currentPhaseId; touch(); }
    public String getCurrentStepId() { return currentStepId; }
    public void setCurrentStepId(String currentStepId) { this.currentStepId = currentStepId; touch(); }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; touch(); }
    public Set<String> getCompletedStepIds() { return Collections.unmodifiableSet(completedStepIds); }
    public List<AgentCheckpoint> getCheckpoints() { return Collections.unmodifiableList(checkpoints); }
    public List<AgentArtifact> getArtifacts() { return Collections.unmodifiableList(artifacts); }
    public Map<String, Object> getAttributes() { return Collections.unmodifiableMap(attributes); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void markCompleted(String stepId) {
        if (stepId != null) {
            completedStepIds.add(stepId);
            touch();
        }
    }

    public void addCheckpoint(AgentCheckpoint checkpoint) {
        if (checkpoint != null) {
            checkpoints.add(checkpoint);
            touch();
        }
    }

    public void addArtifact(AgentArtifact artifact) {
        if (artifact != null) {
            artifacts.add(artifact);
            touch();
        }
    }

    public void putAttribute(String key, Object value) {
        if (key != null) {
            attributes.put(key, value);
            touch();
        }
    }

    private void touch() {
        updatedAt = Instant.now();
    }
}
