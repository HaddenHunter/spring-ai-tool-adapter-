package com.c8software.spring.ai.core.orchestration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConversationContext {

    private String sessionId;

    private String userId;

    private String tenantId;

    private final List<String> messageHistory = new ArrayList<>();

    private String activeTask;

    private final List<ApprovalRequest> pendingApprovals = new ArrayList<>();

    private final Map<String, Object> variables = new LinkedHashMap<>();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getMessageHistory() {
        return messageHistory;
    }

    public String getActiveTask() {
        return activeTask;
    }

    public void setActiveTask(String activeTask) {
        this.activeTask = activeTask;
    }

    public List<ApprovalRequest> getPendingApprovals() {
        return pendingApprovals;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
