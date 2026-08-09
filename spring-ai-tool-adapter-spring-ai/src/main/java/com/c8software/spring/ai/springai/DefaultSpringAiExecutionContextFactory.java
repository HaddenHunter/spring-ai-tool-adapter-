package com.c8software.spring.ai.springai;

import com.c8software.spring.ai.core.execution.ExecutionContext;

import org.springframework.ai.chat.model.ToolContext;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Default mapper for Spring AI tool context values. */
public class DefaultSpringAiExecutionContextFactory implements SpringAiExecutionContextFactory {
    public static final String CURRENT_USER = "currentUser";
    public static final String TENANT_ID = "tenantId";
    public static final String TRACE_ID = "traceId";
    public static final String PERMISSIONS = "permissions";

    public ExecutionContext create(ToolContext toolContext) {
        Map<String, Object> values = toolContext == null ? Collections.<String, Object>emptyMap() : toolContext.getContext();
        return new ExecutionContext(
                stringValue(values.get(CURRENT_USER), "spring-ai"),
                stringValue(values.get(TENANT_ID), "default"),
                stringValue(values.get(TRACE_ID), UUID.randomUUID().toString()),
                permissions(values.get(PERMISSIONS)),
                Instant.now());
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private Set<String> permissions(Object value) {
        if (value instanceof Iterable) {
            Set<String> result = new LinkedHashSet<String>();
            for (Object item : (Iterable<?>) value) {
                if (item != null) {
                    result.add(String.valueOf(item));
                }
            }
            return result;
        }
        if (value instanceof String && !((String) value).trim().isEmpty()) {
            Set<String> result = new LinkedHashSet<String>();
            String[] parts = ((String) value).split(",");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    result.add(part.trim());
                }
            }
            return result;
        }
        return Collections.emptySet();
    }
}
