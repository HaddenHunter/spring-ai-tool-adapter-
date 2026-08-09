package com.c8software.spring.ai.core.execution;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Context passed to tool execution. */
public final class ExecutionContext {
    private final String currentUser;
    private final String tenantId;
    private final String traceId;
    private final Set<String> permissions;
    private final Instant requestTime;

    public ExecutionContext(String currentUser, String tenantId, String traceId, Set<String> permissions, Instant requestTime) {
        this.currentUser = currentUser;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.permissions = Collections.unmodifiableSet(new LinkedHashSet<String>(permissions == null ? Collections.<String>emptySet() : permissions));
        this.requestTime = requestTime == null ? Instant.now() : requestTime;
    }

    public String getCurrentUser() { return currentUser; }
    public String getTenantId() { return tenantId; }
    public String getTraceId() { return traceId; }
    public Set<String> getPermissions() { return permissions; }
    public Instant getRequestTime() { return requestTime; }
}
