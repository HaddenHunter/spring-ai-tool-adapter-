package com.c8software.spring.ai.core.security;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.execution.ExecutionContext;

/** SPI for permission validation. */
public interface PermissionChecker {
    /** Checks access or throws. */
    void check(ToolDefinition definition, ExecutionContext context);
}
