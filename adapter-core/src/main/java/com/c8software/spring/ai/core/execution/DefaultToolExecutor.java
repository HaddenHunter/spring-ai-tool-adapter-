package com.c8software.spring.ai.core.execution;

import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditRecord;
import com.c8software.spring.ai.core.approval.ApprovalDecision;
import com.c8software.spring.ai.core.approval.AutoApproveHumanInTheLoop;
import com.c8software.spring.ai.core.approval.DefaultToolApprovalManager;
import com.c8software.spring.ai.core.approval.ToolApprovalManager;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.context.ContextSnapshot;
import com.c8software.spring.ai.core.context.ConversationContextHolder;
import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.exception.AiToolException;
import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.c8software.spring.ai.core.idempotency.DefaultIdempotencyKeyResolver;
import com.c8software.spring.ai.core.idempotency.IdempotencyKeyResolver;
import com.c8software.spring.ai.core.idempotency.IdempotencyStore;
import com.c8software.spring.ai.core.idempotency.InMemoryIdempotencyStore;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.PermissionChecker;
import com.c8software.spring.ai.core.security.DefaultResultMasker;
import com.c8software.spring.ai.core.security.ResultMasker;
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
    private final ToolApprovalManager approvalManager;
    private final IdempotencyStore idempotencyStore;
    private final IdempotencyKeyResolver idempotencyKeyResolver;
    private final ToolInvocationExecutor invocationExecutor;
    private final ResultMasker resultMasker;

    public DefaultToolExecutor(ToolRegistry registry, PermissionChecker permissionChecker,
                               SensitiveMasker sensitiveMasker, AuditLogger auditLogger,
                               ObjectMapper objectMapper) {
        this(registry, permissionChecker, sensitiveMasker, auditLogger, objectMapper, new AiToolProperties());
    }

    public DefaultToolExecutor(ToolRegistry registry, PermissionChecker permissionChecker,
                               SensitiveMasker sensitiveMasker, AuditLogger auditLogger,
                               ObjectMapper objectMapper, AiToolProperties properties) {
        this(registry, permissionChecker, sensitiveMasker, auditLogger, objectMapper, properties,
                new DefaultToolApprovalManager(new AutoApproveHumanInTheLoop()),
                new InMemoryIdempotencyStore(), new DefaultIdempotencyKeyResolver());
    }

    public DefaultToolExecutor(ToolRegistry registry, PermissionChecker permissionChecker,
                               SensitiveMasker sensitiveMasker, AuditLogger auditLogger,
                               ObjectMapper objectMapper, AiToolProperties properties,
                               ToolApprovalManager approvalManager, IdempotencyStore idempotencyStore,
                               IdempotencyKeyResolver idempotencyKeyResolver) {
        this(registry, permissionChecker, sensitiveMasker, auditLogger, objectMapper, properties,
                approvalManager, idempotencyStore, idempotencyKeyResolver,
                new TimeoutToolInvocationExecutor(), new DefaultResultMasker(sensitiveMasker));
    }

    public DefaultToolExecutor(ToolRegistry registry, PermissionChecker permissionChecker,
                               SensitiveMasker sensitiveMasker, AuditLogger auditLogger,
                               ObjectMapper objectMapper, AiToolProperties properties,
                               ToolApprovalManager approvalManager, IdempotencyStore idempotencyStore,
                               IdempotencyKeyResolver idempotencyKeyResolver,
                               ToolInvocationExecutor invocationExecutor, ResultMasker resultMasker) {
        this.registry = registry;
        this.permissionChecker = permissionChecker;
        this.sensitiveMasker = sensitiveMasker;
        this.auditLogger = auditLogger;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.approvalManager = approvalManager;
        this.idempotencyStore = idempotencyStore;
        this.idempotencyKeyResolver = idempotencyKeyResolver;
        this.invocationExecutor = invocationExecutor;
        this.resultMasker = resultMasker;
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
            String maskedInput = maskInput(definition, args);
            ApprovalDecision approval = approvalManager.approve(definition, executionContext, maskedInput);
            if (!approval.isApproved()) {
                throw new AiToolExecutionException("AIT_APPROVAL_REJECTED",
                        "Tool approval rejected: " + approval.getStatus());
            }
            String idempotencyKey = idempotencyKey(definition, executionContext, args);
            ToolResult cached = cachedResult(definition, idempotencyKey);
            if (cached != null) {
                long cost = System.currentTimeMillis() - start;
                audit(definition, executionContext, maskedInput, String.valueOf(cached.getData()),
                        cost, "IDEMPOTENT_HIT", null, beforeSnapshot);
                return cached;
            }
            Object data = resultMasker.mask(definition, invocationExecutor.invoke(definition, args));
            long cost = System.currentTimeMillis() - start;
            ToolResult result = ToolResult.success(data, cost);
            storeResult(definition, idempotencyKey, result);
            audit(definition, executionContext, maskedInput, String.valueOf(data), cost, "SUCCESS", null, beforeSnapshot);
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

    private String idempotencyKey(ToolDefinition definition, ExecutionContext context, Object[] args) {
        if (!definition.getMetadata().isIdempotent()) {
            return null;
        }
        String tenant = context == null ? "" : context.getTenantId();
        return tenant + ":" + definition.getName() + ":" + idempotencyKeyResolver.resolve(definition, args);
    }

    private ToolResult cachedResult(ToolDefinition definition, String key) {
        if (!definition.getMetadata().isIdempotent() || key == null) {
            return null;
        }
        return idempotencyStore.get(definition.getName(), key);
    }

    private void storeResult(ToolDefinition definition, String key, ToolResult result) {
        if (definition.getMetadata().isIdempotent() && key != null && result != null && result.isSuccess()) {
            idempotencyStore.put(definition.getName(), key, result);
        }
    }
}
