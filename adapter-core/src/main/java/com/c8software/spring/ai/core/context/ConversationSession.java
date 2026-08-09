package com.c8software.spring.ai.core.context;

import java.time.Instant;
import java.util.Map;

public interface ConversationSession {

    String getId();

    String getTenantId();

    String getUserId();

    String getRole();

    String getModelProvider();

    String getModelName();

    Map<String, Object> getAttributes();

    Instant getLastUpdated();

    void touch(Instant instant);
}
