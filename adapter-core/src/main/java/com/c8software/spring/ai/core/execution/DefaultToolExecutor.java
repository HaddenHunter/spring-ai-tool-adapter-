package com.c8software.spring.ai.core.execution;

import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditRecord;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.context.ContextSnapshot;
import com.c8software.spring.ai.core.context.ConversationContextHolder;
import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.exception.AiToolException;
import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.PermissionChecker;
import com.c8software.spring.ai.core.security.SensitiveMasker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Default secure tool executor. */
public class DefaultToolExecutor implements ToolExecutor {
    private final ToolRegistry registry;
    private final PermissionChecker permissionChecker;
    private final SensitiveMasker sensitiveMasker;
    private final AuditLogger auditLogger;
    private final ObjectMapper objectMapper;
    private final AiToolProperties properties;

    public DefaultToolExecutor(ToolRegistry registry, PermissionChecker permissionChecker,
                               SensitiveMasker sensitiveMasker, AuditLogger auditLogger,
                               ObjectMapper objectMapper) {
        this(registry, permissionChecker, sensitiveMasker, auditLogger, objectMapper, new AiToolProperties());
    }

    public DefaultToolExecutor(ToolRegistry registry, PermissionChecker permissionChecker,
                               SensitiveMasker sensitiveMasker, AuditLogger auditLogger,
                               ObjectMapper objectMapper, AiToolProperties properties) {
        this.registry = registry;
        this.permissionChecker = permissionChecker;
        this.sensitiveMasker = sensitiveMasker;
        this.auditLogger = auditLogger;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public ToolResult execute(String toolName, String jsonArguments, ExecutionContext executionContext) {
        long start = System.currentTimeMillis();
        ToolDefinition definition = registry.get(toolName);
        if (definition == null) {
            throw new AiToolExecutionException("AIT_EXEC_404", "Tool not found: " + toolName);
        }
        ContextSnapshot beforeSnapshot = ConversationContextHolder.snapshot();
        try {
            permissionChecker.check(definition, executionContext);
            Object[] args = bindArguments(definition, jsonArguments);
            Object data = definition.getMethodHandle().bindTo(definition.getTargetBean()).invokeWithArguments(args);
            long cost = System.currentTimeMillis() - start;
            ToolResult result = ToolResult.success(data, cost);
            audit(definition, executionContext, maskInput(definition, args), String.valueOf(data), cost, "SUCCESS", null, beforeSnapshot);
            return result;
        } catch (AiToolException ex) {
            long cost = System.currentTimeMillis() - start;
            audit(definition, executionContext, jsonArguments, "", cost, "ERROR", ex.getMessage(), beforeSnapshot);
            if (properties.getFallback().isEnabled()) {
                return ToolResult.failure(ex.getErrorCode(), properties.getFallback().getMessage(), cost);
            }
            throw ex;
        } catch (Throwable ex) {
            Throwable cause = ex instanceof InvocationTargetException ? ((InvocationTargetException) ex).getTargetException() : ex;
            long cost = System.currentTimeMillis() - start;
            audit(definition, executionContext, jsonArguments, "", cost, "ERROR", cause.getMessage(), beforeSnapshot);
            if (properties.getFallback().isEnabled()) {
                return ToolResult.failure("AIT_EXEC_500", properties.getFallback().getMessage(), cost);
            }
            throw new AiToolExecutionException("AIT_EXEC_500", "Tool execution failed: " + cause.getMessage(), cause);
        }
    }

    private Object[] bindArguments(ToolDefinition definition, String jsonArguments) throws Exception {
        Map<String, Object> input = objectMapper.readValue(jsonArguments == null || jsonArguments.trim().isEmpty() ? "{}" : jsonArguments,
                new TypeReference<Map<String, Object>>() {});
        List<Object> args = new ArrayList<Object>();
        for (ToolParameter parameter : definition.getParameters()) {
            Object raw = input.get(parameter.getName());
            if (raw == null && parameter.isRequired()) {
                throw new AiToolExecutionException("AIT_EXEC_400", "Missing required parameter: " + parameter.getName());
            }
            args.add(objectMapper.convertValue(raw, parameter.getType()));
        }
        return args.toArray();
    }

    private String maskInput(ToolDefinition definition, Object[] args) {
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < definition.getParameters().size(); i++) {
            ToolParameter parameter = definition.getParameters().get(i);
            Object value = parameter.getSensitiveType() == null ? args[i] : sensitiveMasker.mask(args[i], parameter.getSensitiveType());
            values.add(parameter.getName() + "=" + value);
        }
        return values.toString();
    }

    private void audit(ToolDefinition definition, ExecutionContext context, String input, String output,
                       long cost, String status, String errorMessage, ContextSnapshot beforeSnapshot) {
        String traceId = context == null ? "" : context.getTraceId();
        String user = context == null ? "" : context.getCurrentUser();
        String tenant = context == null ? "" : context.getTenantId();
        ContextSnapshot afterSnapshot = ConversationContextHolder.snapshot();
        auditLogger.log(new AuditRecord(traceId, definition.getName(), user, tenant,
                String.valueOf(input == null ? 0 : input.hashCode()),
                String.valueOf(output == null ? 0 : output.hashCode()),
                cost, status, errorMessage, "TOOL_CALL",
                snapshotHash(beforeSnapshot), snapshotHash(afterSnapshot), Instant.now()));
    }

    private String snapshotHash(ContextSnapshot snapshot) {
        return snapshot == null ? null : String.valueOf(snapshot.toString().hashCode());
    }
}
