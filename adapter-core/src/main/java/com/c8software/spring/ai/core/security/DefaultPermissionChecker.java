package com.c8software.spring.ai.core.security;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.exception.AiToolSecurityException;
import com.c8software.spring.ai.core.execution.ExecutionContext;

/** Default permission checker based on context permission keys. */
public class DefaultPermissionChecker implements PermissionChecker {
    public void check(ToolDefinition definition, ExecutionContext context) {
        String required = definition.getMetadata().getRequiresPermission();
        if (required == null || required.trim().isEmpty()) {
            return;
        }
        if (context == null || !context.getPermissions().contains(required)) {
            throw new AiToolSecurityException("AIT_SEC_001", "Permission denied for tool: " + definition.getName());
        }
    }
}
