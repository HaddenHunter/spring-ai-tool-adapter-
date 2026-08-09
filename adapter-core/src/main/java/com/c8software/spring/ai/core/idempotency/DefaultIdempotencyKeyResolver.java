package com.c8software.spring.ai.core.idempotency;

import com.c8software.spring.ai.core.definition.ToolDefinition;

public class DefaultIdempotencyKeyResolver implements IdempotencyKeyResolver {

    public String resolve(ToolDefinition definition, Object[] args) {
        String expression = definition.getMetadata().getIdempotentKey();
        if (expression == null || expression.trim().isEmpty()) {
            return definition.getName();
        }
        String key = expression;
        for (int i = 0; i < definition.getParameters().size(); i++) {
            Object value = args == null || args.length <= i ? null : args[i];
            key = key.replace("#" + definition.getParameters().get(i).getName(), String.valueOf(value));
        }
        return key;
    }
}
