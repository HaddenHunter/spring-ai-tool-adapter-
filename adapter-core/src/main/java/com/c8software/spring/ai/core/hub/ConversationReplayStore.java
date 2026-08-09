package com.c8software.spring.ai.core.hub;

import java.util.List;

/** Stores replayable business conversation turns. */
public interface ConversationReplayStore {
    void append(ConversationTurn turn);

    List<ConversationTurn> list(String tenantId, String sessionId, int limit);
}
