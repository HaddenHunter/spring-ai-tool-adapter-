package com.c8software.spring.ai.core.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryConversationSessionStore implements ConversationSessionStore {

    private final ConcurrentMap<String, ConversationSession> sessions = new ConcurrentHashMap<String, ConversationSession>();

    public ConversationSession get(String tenantId, String sessionId) {
        return sessions.get(key(tenantId, sessionId));
    }

    public void save(ConversationSession session) {
        if (session != null) {
            sessions.put(key(session.getTenantId(), session.getId()), session);
        }
    }

    public void reset(String tenantId, String sessionId) {
        sessions.remove(key(tenantId, sessionId));
    }

    private String key(String tenantId, String sessionId) {
        return String.valueOf(tenantId) + ":" + String.valueOf(sessionId);
    }
}
