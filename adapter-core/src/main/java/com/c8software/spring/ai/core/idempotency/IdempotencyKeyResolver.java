package com.c8software.spring.ai.core.idempotency;

import com.c8software.spring.ai.core.definition.ToolDefinition;

public interface IdempotencyKeyResolver {

    String resolve(ToolDefinition definition, Object[] args);
}
