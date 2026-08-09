package com.c8software.spring.ai.core.enterprise;

import java.util.List;

/** Stores feedback for prompt, tool, and task quality loops. */
public interface LearningFeedbackStore {
    void record(FeedbackSignal signal);

    List<FeedbackSignal> list(String tenantId, int limit);
}
