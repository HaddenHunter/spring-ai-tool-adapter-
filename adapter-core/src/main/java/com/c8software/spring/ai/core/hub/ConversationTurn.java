package com.c8software.spring.ai.core.hub;

import com.c8software.spring.ai.core.context.ContextSnapshot;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Replayable record for one user turn handled by the business AI hub. */
public final class ConversationTurn {
    private final String id;
    private final String sessionId;
    private final String tenantId;
    private final String userId;
    private final String userInput;
    private final String selectedTool;
    private final String status;
    private final ContextSnapshot beforeSnapshot;
    private final ContextSnapshot afterSnapshot;
    private final Map<String, Object> attributes;
    private final Instant createdAt;

    public ConversationTurn(String id, String sessionId, String tenantId, String userId, String userInput,
                            String selectedTool, String status, ContextSnapshot beforeSnapshot,
                            ContextSnapshot afterSnapshot, Map<String, Object> attributes, Instant createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.userInput = userInput;
        this.selectedTool = selectedTool;
        this.status = status;
        this.beforeSnapshot = beforeSnapshot;
        this.afterSnapshot = afterSnapshot;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(
                attributes == null ? Collections.<String, Object>emptyMap() : attributes));
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getTenantId() { return tenantId; }
    public String getUserId() { return userId; }
    public String getUserInput() { return userInput; }
    public String getSelectedTool() { return selectedTool; }
    public String getStatus() { return status; }
    public ContextSnapshot getBeforeSnapshot() { return beforeSnapshot; }
    public ContextSnapshot getAfterSnapshot() { return afterSnapshot; }
    public Map<String, Object> getAttributes() { return attributes; }
    public Instant getCreatedAt() { return createdAt; }
}
