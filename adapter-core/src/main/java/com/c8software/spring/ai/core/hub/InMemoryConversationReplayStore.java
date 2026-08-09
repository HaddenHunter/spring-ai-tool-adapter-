package com.c8software.spring.ai.core.hub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory replay store for demos and tests. */
public class InMemoryConversationReplayStore implements ConversationReplayStore {
    private final List<ConversationTurn> turns = new CopyOnWriteArrayList<ConversationTurn>();

    public void append(ConversationTurn turn) {
        if (turn != null) {
            turns.add(turn);
        }
    }

    public List<ConversationTurn> list(String tenantId, String sessionId, int limit) {
        int max = limit <= 0 ? 100 : limit;
        List<ConversationTurn> result = new ArrayList<ConversationTurn>();
        for (int i = turns.size() - 1; i >= 0 && result.size() < max; i--) {
            ConversationTurn turn = turns.get(i);
            if (same(tenantId, turn.getTenantId()) && same(sessionId, turn.getSessionId())) {
                result.add(turn);
            }
        }
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }

    private boolean same(String expected, String actual) {
        return expected == null ? actual == null : expected.equals(actual);
    }
}
