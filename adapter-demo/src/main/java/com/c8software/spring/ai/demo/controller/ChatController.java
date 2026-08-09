package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.execution.ToolResult;
import com.c8software.spring.ai.core.mcp.McpProvisionPlan;
import com.c8software.spring.ai.core.mcp.McpProvisioningPlanner;
import com.c8software.spring.ai.core.mcp.McpSemanticRequest;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.schema.OpenAIFunctionSchemaConverter;
import com.c8software.spring.ai.core.visibility.ToolVisibilityFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ChatController {

    private final ToolRegistry registry;

    private final ToolExecutor executor;

    private final McpProvisioningPlanner mcpProvisioningPlanner;

    private final ToolVisibilityFilter visibilityFilter;

    private final MetricsCollector metricsCollector;

    private final OpenAIFunctionSchemaConverter schemaConverter = new OpenAIFunctionSchemaConverter();

    public ChatController(ToolRegistry registry, ToolExecutor executor, McpProvisioningPlanner mcpProvisioningPlanner,
                          ToolVisibilityFilter visibilityFilter, MetricsCollector metricsCollector) {
        this.registry = registry;
        this.executor = executor;
        this.mcpProvisioningPlanner = mcpProvisioningPlanner;
        this.visibilityFilter = visibilityFilter;
        this.metricsCollector = metricsCollector;
    }

    @GetMapping({"/", "/chat"})
    public String chat() {
        return "chat";
    }

    @GetMapping("/api/tools")
    @ResponseBody
    public List<Map<String, Object>> tools() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ToolDefinition definition : visibilityFilter.filter(registry.listAll(), demoExecutionContext())) {
            result.add(toToolDto(definition));
        }
        return result;
    }

    @GetMapping("/api/debug/schema")
    @ResponseBody
    public List<Map<String, Object>> schemas() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ToolDefinition definition : visibilityFilter.filter(registry.listAll(), demoExecutionContext())) {
            result.add(schemaConverter.convert(definition));
        }
        return result;
    }

    @GetMapping("/api/debug/prompt")
    @ResponseBody
    public Map<String, Object> prompt() {
        StringBuilder builder = new StringBuilder();
        builder.append("You are an enterprise AI assistant.\n");
        builder.append("Use only audited tools exposed by the adapter.\n");
        builder.append("Preserve confirmed user choices in structured context.\n");
        builder.append("Available tools:\n");
        for (ToolDefinition definition : visibilityFilter.filter(registry.listAll(), demoExecutionContext())) {
            builder.append("- ")
                    .append(definition.getName())
                    .append(": ")
                    .append(definition.getDescription())
                    .append('\n');
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prompt", builder.toString());
        return result;
    }

    @GetMapping("/api/governance")
    @ResponseBody
    public Map<String, Object> governance() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentUser", "demo-user");
        result.put("tenantId", "demo-tenant");
        result.put("permissionScopes", Arrays.asList("demo:tool:invoke", "finance:read"));
        result.put("tokenUsed", 1280);
        result.put("tokenBudget", 8192);
        return result;
    }

    @GetMapping("/api/v0/status")
    @ResponseBody
    public Map<String, Object> v0Status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("versionLine", "v0.x");
        result.put("status", "COMPLETE");
        result.put("capabilities", Arrays.asList(
                "annotation registration",
                "tool registry",
                "multi-provider schema conversion",
                "governed execution",
                "timeout isolation",
                "input and return-value masking",
                "audit logging",
                "visibility filtering",
                "idempotency",
                "human approval boundary",
                "chat ui",
                "debug endpoints",
                "prometheus metrics",
                "spring boot starter",
                "maven publishing"
        ));
        result.put("toolCount", visibilityFilter.filter(registry.listAll(), demoExecutionContext()).size());
        result.put("prometheusEndpoint", "/actuator/prometheus");
        return result;
    }

    @PostMapping("/api/mcp/semantic-plan")
    @ResponseBody
    public McpProvisionPlan semanticMcpPlan(@RequestBody Map<String, String> body) {
        String utterance = body.getOrDefault("utterance", "");
        return mcpProvisioningPlanner.plan(new McpSemanticRequest(
                UUID.randomUUID().toString(),
                "demo-tenant",
                "demo-user",
                utterance,
                Arrays.asList("mcp:provision:crm", "mcp:provision:messaging")
        ));
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public Map<String, Object> chat(@RequestBody Map<String, String> body) {
        String toolName = body.getOrDefault("toolName", "mock_query_weather");
        String arguments = body.getOrDefault("arguments", "{}");
        metricsCollector.activeConversations().incrementAndGet();
        ExecutionContext context = new ExecutionContext(
                "demo-user",
                "demo-tenant",
                UUID.randomUUID().toString(),
                new LinkedHashSet<>(Arrays.asList("demo:tool:invoke", "finance:read")),
                Instant.now()
        );
        try {
            ToolResult toolResult = executor.execute(toolName, arguments, context);
            metricsCollector.recordToolCall();
            metricsCollector.recordTokenUsage(estimateTokens(arguments));
            if (!toolResult.isSuccess()) {
                metricsCollector.recordToolError();
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message", toolResult.isSuccess() ? "Tool executed" : "Tool failed");
            result.put("toolName", toolName);
            result.put("result", toolResult);
            return result;
        } catch (RuntimeException ex) {
            metricsCollector.recordToolCall();
            metricsCollector.recordToolError();
            throw ex;
        } finally {
            metricsCollector.activeConversations().decrementAndGet();
        }
    }

    @GetMapping("/api/chat/stream")
    public SseEmitter stream() throws IOException {
        SseEmitter emitter = new SseEmitter();
        emitter.send(SseEmitter.event().name("message").data("demo stream ready"));
        emitter.complete();
        return emitter;
    }

    private ExecutionContext demoExecutionContext() {
        return new ExecutionContext(
                "demo-user",
                "demo-tenant",
                UUID.randomUUID().toString(),
                new LinkedHashSet<>(Arrays.asList("demo:tool:invoke", "finance:read")),
                Instant.now()
        );
    }

    private Map<String, Object> toToolDto(ToolDefinition definition) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("name", definition.getName());
        dto.put("description", definition.getDescription());
        dto.put("group", definition.getMetadata().getGroup());
        dto.put("auditLevel", definition.getMetadata().getAuditLevel());
        dto.put("requiresPermission", definition.getMetadata().getRequiresPermission());
        dto.put("riskLevel", definition.getMetadata().getRiskLevel());
        dto.put("visibility", definition.getMetadata().getVisibility());
        dto.put("version", definition.getMetadata().getVersion());
        dto.put("idempotent", definition.getMetadata().isIdempotent());
        dto.put("idempotentKey", definition.getMetadata().getIdempotentKey());
        dto.put("rollbackable", definition.getMetadata().isRollbackable());
        dto.put("rollbackMethod", definition.getMetadata().getRollbackMethod());
        dto.put("contextKey", definition.getMetadata().getContextKey());
        dto.put("contextConfirmed", definition.getMetadata().isContextConfirmed());
        dto.put("resultSensitiveType", definition.getMetadata().getResultSensitiveType());
        dto.put("timeoutMillis", definition.getMetadata().getTimeoutMillis());
        dto.put("parameters", parameterDtos(definition.getParameters()));
        return dto;
    }

    private double estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 1.0d;
        }
        return Math.max(1.0d, Math.ceil(text.length() / 4.0d));
    }

    private List<Map<String, Object>> parameterDtos(List<ToolParameter> parameters) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ToolParameter parameter : parameters) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("name", parameter.getName());
            dto.put("description", parameter.getDescription());
            dto.put("type", parameter.getType().getSimpleName());
            dto.put("required", parameter.isRequired());
            dto.put("sensitiveType", parameter.getSensitiveType());
            dto.put("contextKey", parameter.getContextKey());
            dto.put("contextConfirmed", parameter.isContextConfirmed());
            result.add(dto);
        }
        return result;
    }
}
