package com.c8software.spring.ai.core.execution;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Context passed to tool execution. */
public final class ExecutionContext {
    private final String currentUser;
    private final String tenantId;
    private final String traceId;
    private final Set<String> permissions;
    private final Instant requestTime;
    private final Map<String, Object> attributes;

    public ExecutionContext(String currentUser, String tenantId, String traceId, Set<String> permissions, Instant requestTime) {
        this(currentUser, tenantId, traceId, permissions, requestTime, Collections.<String, Object>emptyMap());
    }

    public ExecutionContext(String currentUser, String tenantId, String traceId, Set<String> permissions,
                            Instant requestTime, Map<String, Object> attributes) {
        this.currentUser = currentUser;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.permissions = Collections.unmodifiableSet(new LinkedHashSet<String>(permissions == null ? Collections.<String>emptySet() : permissions));
        this.requestTime = requestTime == null ? Instant.now() : requestTime;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(
                attributes == null ? Collections.<String, Object>emptyMap() : attributes));
    }

    public String getCurrentUser() { return currentUser; }
    public String getTenantId() { return tenantId; }
    public String getTraceId() { return traceId; }
    public Set<String> getPermissions() { return permissions; }
    public Instant getRequestTime() { return requestTime; }
    public Map<String, Object> getAttributes() { return attributes; }
    public Object getAttribute(String name) { return attributes.get(name); }
}
