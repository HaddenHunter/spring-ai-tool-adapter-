package com.c8software.spring.ai.core.definition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable metadata attached to a tool. */
public final class ToolMetadata {
    private final String group;
    private final String requiresPermission;
    private final String auditLevel;
    private final String riskLevel;
    private final String visibility;
    private final String version;
    private final boolean idempotent;
    private final String idempotentKey;
    private final boolean rollbackable;
    private final String rollbackMethod;
    private final String contextKey;
    private final boolean contextConfirmed;
    private final boolean enabled;
    private final long timeoutMillis;
    private final Map<String, String> attributes;

    private ToolMetadata(Builder builder) {
        this.group = builder.group;
        this.requiresPermission = builder.requiresPermission;
        this.auditLevel = builder.auditLevel;
        this.riskLevel = builder.riskLevel;
        this.visibility = builder.visibility;
        this.version = builder.version;
        this.idempotent = builder.idempotent;
        this.idempotentKey = builder.idempotentKey;
        this.rollbackable = builder.rollbackable;
        this.rollbackMethod = builder.rollbackMethod;
        this.contextKey = builder.contextKey;
        this.contextConfirmed = builder.contextConfirmed;
        this.enabled = builder.enabled;
        this.timeoutMillis = builder.timeoutMillis;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<String, String>(builder.attributes));
    }

    /** Creates a metadata builder. */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns group name. */
    public String getGroup() {
        return group;
    }

    /** Returns permission expression. */
    public String getRequiresPermission() {
        return requiresPermission;
    }

    /** Returns audit level. */
    public String getAuditLevel() {
        return auditLevel;
    }

    /** Returns business risk level. */
    public String getRiskLevel() {
        return riskLevel;
    }

    /** Returns tool visibility. */
    public String getVisibility() {
        return visibility;
    }

    /** Returns tool contract version. */
    public String getVersion() {
        return version;
    }

    /** Returns whether the tool is idempotent. */
    public boolean isIdempotent() {
        return idempotent;
    }

    /** Returns idempotency key expression. */
    public String getIdempotentKey() {
        return idempotentKey;
    }

    /** Returns whether the tool declares a rollback method. */
    public boolean isRollbackable() {
        return rollbackable;
    }

    /** Returns rollback method name. */
    public String getRollbackMethod() {
        return rollbackMethod;
    }

    /** Returns result context key binding. */
    public String getContextKey() {
        return contextKey;
    }

    /** Returns whether the context binding is a confirmed fact. */
    public boolean isContextConfirmed() {
        return contextConfirmed;
    }

    /** Returns whether enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Returns timeout in milliseconds. */
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    /** Returns extra attributes. */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /** Builder for immutable metadata. */
    public static final class Builder {
        private String group = "default";
        private String requiresPermission = "";
        private String auditLevel = "BASIC";
        private String riskLevel = "LOW";
        private String visibility = "PUBLIC";
        private String version = "1.0.0";
        private boolean idempotent;
        private String idempotentKey = "";
        private boolean rollbackable;
        private String rollbackMethod = "";
        private String contextKey = "";
        private boolean contextConfirmed;
        private boolean enabled = true;
        private long timeoutMillis = 10000L;
        private final Map<String, String> attributes = new LinkedHashMap<String, String>();

        public Builder group(String group) {
            this.group = group == null || group.trim().isEmpty() ? "default" : group;
            return this;
        }

        public Builder requiresPermission(String requiresPermission) {
            this.requiresPermission = requiresPermission == null ? "" : requiresPermission;
            return this;
        }

        public Builder auditLevel(String auditLevel) {
            this.auditLevel = auditLevel == null || auditLevel.trim().isEmpty() ? "BASIC" : auditLevel;
            return this;
        }

        public Builder riskLevel(String riskLevel) {
            this.riskLevel = riskLevel == null || riskLevel.trim().isEmpty() ? "LOW" : riskLevel;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility == null || visibility.trim().isEmpty() ? "PUBLIC" : visibility;
            return this;
        }

        public Builder version(String version) {
            this.version = version == null || version.trim().isEmpty() ? "1.0.0" : version;
            return this;
        }

        public Builder idempotent(boolean idempotent) {
            this.idempotent = idempotent;
            return this;
        }

        public Builder idempotentKey(String idempotentKey) {
            this.idempotentKey = idempotentKey == null ? "" : idempotentKey;
            return this;
        }

        public Builder rollbackable(boolean rollbackable) {
            this.rollbackable = rollbackable;
            return this;
        }

        public Builder rollbackMethod(String rollbackMethod) {
            this.rollbackMethod = rollbackMethod == null ? "" : rollbackMethod;
            return this;
        }

        public Builder contextKey(String contextKey) {
            this.contextKey = contextKey == null ? "" : contextKey;
            return this;
        }

        public Builder contextConfirmed(boolean contextConfirmed) {
            this.contextConfirmed = contextConfirmed;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder timeoutMillis(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis <= 0 ? 10000L : timeoutMillis;
            return this;
        }

        public Builder attribute(String key, String value) {
            if (key != null && value != null) {
                this.attributes.put(key, value);
            }
            return this;
        }

        public ToolMetadata build() {
            return new ToolMetadata(this);
        }
    }
}
