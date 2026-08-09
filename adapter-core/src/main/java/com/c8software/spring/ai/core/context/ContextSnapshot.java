package com.c8software.spring.ai.core.context;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ContextSnapshot {

    private final String sessionId;
    private final String tenantId;
    private final String userId;
    private final String role;
    private final String modelProvider;
    private final String modelName;
    private final String taskId;
    private final String taskType;
    private final TaskStatus taskStatus;
    private final String currentStep;
    private final boolean pendingApproval;
    private final Map<String, Object> facts;
    private final Map<String, Object> userOverrides;
    private final Instant capturedAt;

    public ContextSnapshot(ConversationSession session, TaskContext taskContext, Instant capturedAt) {
        this.sessionId = session == null ? null : session.getId();
        this.tenantId = session == null ? null : session.getTenantId();
        this.userId = session == null ? null : session.getUserId();
        this.role = session == null ? null : session.getRole();
        this.modelProvider = session == null ? null : session.getModelProvider();
        this.modelName = session == null ? null : session.getModelName();
        this.taskId = taskContext == null ? null : taskContext.getTaskId();
        this.taskType = taskContext == null ? null : taskContext.getTaskType();
        this.taskStatus = taskContext == null ? null : taskContext.getTaskStatus();
        this.currentStep = taskContext == null ? null : taskContext.getCurrentStep();
        this.pendingApproval = taskContext != null && taskContext.isPendingApproval();
        this.facts = Collections.unmodifiableMap(copyFactValues(taskContext));
        this.userOverrides = Collections.unmodifiableMap(copyUserOverrides(taskContext));
        this.capturedAt = capturedAt == null ? Instant.now() : capturedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getModelProvider() {
        return modelProvider;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public boolean isPendingApproval() {
        return pendingApproval;
    }

    public Map<String, Object> getFacts() {
        return facts;
    }

    public Map<String, Object> getUserOverrides() {
        return userOverrides;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    private Map<String, Object> copyFactValues(TaskContext taskContext) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        if (taskContext != null) {
            for (Map.Entry<String, ContextFact> entry : taskContext.getFacts().entrySet()) {
                values.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        return values;
    }

    private Map<String, Object> copyUserOverrides(TaskContext taskContext) {
        return taskContext == null
                ? new LinkedHashMap<String, Object>()
                : new LinkedHashMap<String, Object>(taskContext.getUserOverrides());
    }

    @Override
    public String toString() {
        return "ContextSnapshot{"
                + "sessionId='" + sessionId + '\''
                + ", tenantId='" + tenantId + '\''
                + ", userId='" + userId + '\''
                + ", role='" + role + '\''
                + ", modelProvider='" + modelProvider + '\''
                + ", modelName='" + modelName + '\''
                + ", taskId='" + taskId + '\''
                + ", taskType='" + taskType + '\''
                + ", taskStatus=" + taskStatus
                + ", currentStep='" + currentStep + '\''
                + ", pendingApproval=" + pendingApproval
                + ", facts=" + facts
                + ", userOverrides=" + userOverrides
                + '}';
    }
}
