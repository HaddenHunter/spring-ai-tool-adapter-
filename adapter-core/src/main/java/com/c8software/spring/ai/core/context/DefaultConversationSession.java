package com.c8software.spring.ai.core.context;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultConversationSession implements ConversationSession {

    private final String id;
    private final String tenantId;
    private final String userId;
    private final String role;
    private final String modelProvider;
    private final String modelName;
    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
    private Instant lastUpdated;

    public DefaultConversationSession(String id, String tenantId, String userId, String role,
                                      String modelProvider, String modelName, Instant lastUpdated) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.role = role;
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        this.lastUpdated = lastUpdated == null ? Instant.now() : lastUpdated;
    }

    public String getId() {
        return id;
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

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public void putAttribute(String key, Object value) {
        if (key != null) {
            attributes.put(key, value);
            touch(Instant.now());
        }
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void touch(Instant instant) {
        this.lastUpdated = instant == null ? Instant.now() : instant;
    }
}
