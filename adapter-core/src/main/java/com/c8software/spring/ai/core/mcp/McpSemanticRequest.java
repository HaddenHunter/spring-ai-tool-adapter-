package com.c8software.spring.ai.core.mcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class McpSemanticRequest {

    private final String sessionId;
    private final String tenantId;
    private final String userId;
    private final String utterance;
    private final List<String> permissions;

    public McpSemanticRequest(String sessionId, String tenantId, String userId, String utterance, List<String> permissions) {
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.utterance = utterance;
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(permissions == null
                ? Collections.<String>emptyList()
                : permissions));
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

    public String getUtterance() {
        return utterance;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
