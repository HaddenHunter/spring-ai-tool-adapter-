package com.c8software.spring.ai.core.enterprise;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Tenant isolation profile for enterprise deployments. */
public final class TenantProfile {
    private final String tenantId;
    private final String name;
    private final String deploymentMode;
    private final Set<String> enabledToolGroups;
    private final Instant createdAt;

    public TenantProfile(String tenantId, String name, String deploymentMode,
                         Set<String> enabledToolGroups, Instant createdAt) {
        this.tenantId = tenantId;
        this.name = name;
        this.deploymentMode = deploymentMode == null || deploymentMode.trim().isEmpty() ? "private" : deploymentMode;
        this.enabledToolGroups = Collections.unmodifiableSet(new LinkedHashSet<String>(
                enabledToolGroups == null ? Collections.<String>emptySet() : enabledToolGroups));
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getTenantId() { return tenantId; }
    public String getName() { return name; }
    public String getDeploymentMode() { return deploymentMode; }
    public Set<String> getEnabledToolGroups() { return enabledToolGroups; }
    public Instant getCreatedAt() { return createdAt; }
}
