package com.c8software.spring.ai.core.enterprise;

import java.time.Instant;

/** Feedback signal used by self-learning loops. */
public final class FeedbackSignal {
    private final String id;
    private final String tenantId;
    private final String targetType;
    private final String targetId;
    private final int score;
    private final String comment;
    private final Instant createdAt;

    public FeedbackSignal(String id, String tenantId, String targetType, String targetId,
                          int score, String comment, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
