package com.c8software.spring.ai.core.registry;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolAudit;
import com.c8software.spring.ai.core.annotation.AiToolContextKey;
import com.c8software.spring.ai.core.annotation.AiToolIdempotent;
import com.c8software.spring.ai.core.annotation.AiToolRequiresPermission;
import com.c8software.spring.ai.core.annotation.AiToolRiskLevel;
import com.c8software.spring.ai.core.annotation.AiToolRollback;
import com.c8software.spring.ai.core.annotation.AiToolSensitive;
import com.c8software.spring.ai.core.annotation.AiToolVersion;
import com.c8software.spring.ai.core.annotation.AiToolVisibility;
import com.c8software.spring.ai.core.annotation.Rollbackable;
import com.c8software.spring.ai.core.definition.ToolMetadata;

import java.lang.reflect.Method;

public class DefaultToolGovernanceAnnotationProcessor implements ToolGovernanceAnnotationProcessor {

    public ToolMetadata.Builder enrich(Method method, AiTool aiTool, ToolMetadata.Builder builder) {
        AiToolRequiresPermission permission = method.getAnnotation(AiToolRequiresPermission.class);
        AiToolRiskLevel riskLevel = method.getAnnotation(AiToolRiskLevel.class);
        AiToolAudit audit = method.getAnnotation(AiToolAudit.class);
        AiToolIdempotent idempotent = method.getAnnotation(AiToolIdempotent.class);
        AiToolRollback rollback = method.getAnnotation(AiToolRollback.class);
        Rollbackable legacyRollback = method.getAnnotation(Rollbackable.class);
        AiToolVisibility visibility = method.getAnnotation(AiToolVisibility.class);
        AiToolVersion version = method.getAnnotation(AiToolVersion.class);
        AiToolContextKey contextKey = method.getAnnotation(AiToolContextKey.class);
        AiToolSensitive resultSensitive = method.getAnnotation(AiToolSensitive.class);

        builder.requiresPermission(permission == null ? aiTool.requiresPermission() : permission.value());
        builder.auditLevel(audit == null ? aiTool.auditLevel().name() : audit.value().name());
        if (riskLevel != null) {
            builder.riskLevel(riskLevel.value().name());
        }
        if (idempotent != null) {
            builder.idempotent(true).idempotentKey(idempotent.key());
        }
        if (rollback != null) {
            builder.rollbackable(true).rollbackMethod(rollback.rollbackMethod());
        } else if (legacyRollback != null) {
            builder.rollbackable(true).rollbackMethod(legacyRollback.value());
        }
        if (visibility != null) {
            builder.visibility(visibility.value().name());
        }
        if (version != null) {
            builder.version(version.value());
        }
        if (contextKey != null) {
            builder.contextKey(contextKey.store()).contextConfirmed(contextKey.confirmed());
        }
        if (resultSensitive != null) {
            builder.resultSensitiveType(resultSensitive.type().name());
        }
        return builder;
    }
}
