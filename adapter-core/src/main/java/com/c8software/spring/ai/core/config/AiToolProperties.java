package com.c8software.spring.ai.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/** Externalized configuration for AI tool registration and execution. */
@ConfigurationProperties(prefix = "ai.tool")
public class AiToolProperties {
    private boolean enabled = true;
    private long defaultTimeoutMillis = 10000L;
    private Map<String, Boolean> tools = new LinkedHashMap<String, Boolean>();
    private final Fallback fallback = new Fallback();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getDefaultTimeoutMillis() { return defaultTimeoutMillis; }
    public void setDefaultTimeoutMillis(long defaultTimeoutMillis) { this.defaultTimeoutMillis = defaultTimeoutMillis; }
    public Map<String, Boolean> getTools() { return tools; }
    public void setTools(Map<String, Boolean> tools) { this.tools = tools; }
    public Fallback getFallback() { return fallback; }

    /** Returns whether a named tool is enabled. */
    public boolean isToolEnabled(String toolName, boolean annotationEnabled) {
        if (!enabled || !annotationEnabled) {
            return false;
        }
        Boolean item = tools.get(toolName);
        return item == null || item.booleanValue();
    }

    /** Fallback behavior for tool execution failures. */
    public static class Fallback {
        private boolean enabled;
        private String message = "This tool is temporarily unavailable. Please try again later.";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
