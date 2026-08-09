package com.c8software.spring.ai.core.enterprise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory feedback store for demos and tests. */
public class InMemoryLearningFeedbackStore implements LearningFeedbackStore {
    private final List<FeedbackSignal> signals = new CopyOnWriteArrayList<FeedbackSignal>();

    public void record(FeedbackSignal signal) {
        if (signal != null) {
            signals.add(signal);
        }
    }

    public List<FeedbackSignal> list(String tenantId, int limit) {
        int max = limit <= 0 ? 100 : limit;
        List<FeedbackSignal> result = new ArrayList<FeedbackSignal>();
        for (int i = signals.size() - 1; i >= 0 && result.size() < max; i--) {
            FeedbackSignal signal = signals.get(i);
            if (tenantId == null || tenantId.equals(signal.getTenantId())) {
                result.add(signal);
            }
        }
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }
}
