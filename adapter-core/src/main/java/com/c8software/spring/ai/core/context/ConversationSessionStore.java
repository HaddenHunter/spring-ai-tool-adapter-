package com.c8software.spring.ai.core.context;

public interface ConversationSessionStore {

    ConversationSession get(String tenantId, String sessionId);

    void save(ConversationSession session);

    void reset(String tenantId, String sessionId);
}
