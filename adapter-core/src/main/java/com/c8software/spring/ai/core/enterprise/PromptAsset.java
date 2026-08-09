package com.c8software.spring.ai.core.enterprise;

import java.time.Instant;

/** Versioned prompt asset for the enterprise prompt marketplace. */
public final class PromptAsset {
    private final String id;
    private final String name;
    private final String version;
    private final String owner;
    private final String status;
    private final String content;
    private final Instant updatedAt;

    public PromptAsset(String id, String name, String version, String owner,
                       String status, String content, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.owner = owner;
        this.status = status == null || status.trim().isEmpty() ? "DRAFT" : status;
        this.content = content;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getOwner() { return owner; }
    public String getStatus() { return status; }
    public String getContent() { return content; }
    public Instant getUpdatedAt() { return updatedAt; }
}
