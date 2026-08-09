package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class SpringAiChatClientDemoService {

    private final ToolRegistry registry;
    private final ToolCallbackProvider toolCallbackProvider;
    private final DemoApprovalStore approvals;
    private final ObjectMapper objectMapper;

    public SpringAiChatClientDemoService(ToolRegistry registry, ToolCallbackProvider toolCallbackProvider,
                                         DemoApprovalStore approvals, ObjectMapper objectMapper) {
        this.registry = registry;
        this.toolCallbackProvider = toolCallbackProvider;
        this.approvals = approvals;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> chat(String sessionId, String utterance) {
        PlannedToolCall plan = plan(utterance);
        ToolDefinition definition = registry.get(plan.getToolName());
        if (definition == null) {
            return message("ERROR", "No governed tool matched the user request.", plan, null);
        }
        if (requiresApproval(definition)) {
            DemoApprovalStore.PendingApproval approval = approvals.create(sessionId, utterance,
                    plan.getToolName(), plan.getArgumentsJson(), definition.getMetadata().getRiskLevel(),
                    "Spring AI planned a high-risk governed tool call.");
            Map<String, Object> response = message("PENDING_APPROVAL",
                    "Spring AI selected a high-risk tool. Human approval is required before execution.",
                    plan, null);
            response.put("approval", approvals.toDto(approval));
            return response;
        }
        return executePlan(plan, "EXECUTED");
    }

    public Map<String, Object> approve(String approvalId) {
        DemoApprovalStore.PendingApproval approval = approvals.get(approvalId);
        if (approval == null) {
            return message("ERROR", "Approval request not found.", null, null);
        }
        approvals.markApproved(approvalId);
        Map<String, Object> response = executePlan(new PlannedToolCall(approval.getToolName(), approval.getArgumentsJson(), approvalId),
                "APPROVED_AND_EXECUTED");
        response.put("approval", approvals.toDto(approval));
        return response;
    }

    public Map<String, Object> reject(String approvalId) {
        DemoApprovalStore.PendingApproval approval = approvals.get(approvalId);
        if (approval == null) {
            return message("ERROR", "Approval request not found.", null, null);
        }
        approvals.markRejected(approvalId);
        Map<String, Object> response = message("REJECTED", "Human rejected the high-risk tool call.",
                new PlannedToolCall(approval.getToolName(), approval.getArgumentsJson()), null);
        response.put("approval", approvals.toDto(approval));
        return response;
    }

    private Map<String, Object> executePlan(PlannedToolCall plan, String status) {
        ToolCallback callback = callback(plan.getToolName());
        String toolResultJson = callback == null
                ? "{\"success\":false,\"errorMessage\":\"ToolCallback not found\"}"
                : callback.call(plan.getArgumentsJson(), toolContext(plan.getApprovalId()));
        return message(status, "Spring AI ToolCallback executed through the governed adapter.", plan, toolResultJson);
    }

    private ToolCallback callback(String toolName) {
        for (ToolCallback callback : toolCallbackProvider.getToolCallbacks()) {
            if (callback.getToolDefinition().name().equals(toolName)) {
                return callback;
            }
        }
        return null;
    }

    private ToolContext toolContext(String approvalId) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("currentUser", "demo-user");
        values.put("tenantId", "demo-tenant");
        values.put("traceId", UUID.randomUUID().toString());
        values.put("permissions", new LinkedHashSet<String>(Arrays.asList("demo:tool:invoke", "finance:read")));
        if (approvalId != null && !approvalId.trim().isEmpty()) {
            values.put("approvalId", approvalId);
        }
        return new ToolContext(values);
    }

    private PlannedToolCall plan(String utterance) {
        String text = utterance == null ? "" : utterance.toLowerCase(Locale.ENGLISH);
        if (text.contains("order") || text.contains("create")) {
            return new PlannedToolCall("mock_create_order", "{\"userId\":\"1001\",\"amount\":199.9}");
        }
        if (text.contains("sms")) {
            return new PlannedToolCall("mock_send_sms", "{\"mobile\":\"13800138000\",\"content\":\"Your service ticket has been updated.\"}");
        }
        if (text.contains("balance")) {
            return new PlannedToolCall("mock_query_user_balance", "{\"userId\":\"1001\"}");
        }
        return new PlannedToolCall("mock_query_weather", "{\"city\":\"Shanghai\"}");
    }

    private boolean requiresApproval(ToolDefinition definition) {
        String risk = definition.getMetadata().getRiskLevel();
        if (risk == null) {
            return false;
        }
        String normalized = risk.toUpperCase(Locale.ENGLISH);
        return "HIGH".equals(normalized) || "CRITICAL".equals(normalized);
    }

    private Map<String, Object> message(String status, String assistantMessage, PlannedToolCall plan, String toolResultJson) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("channel", "spring-ai-chat-client-demo");
        response.put("status", status);
        response.put("assistantMessage", assistantMessage);
        response.put("toolCallbacks", toolCallbackProvider.getToolCallbacks().length);
        if (plan != null) {
            response.put("plannedToolName", plan.getToolName());
            response.put("plannedArguments", plan.getArgumentsJson());
            response.put("toolMetadata", toolMetadata(plan.getToolName()));
        }
        if (toolResultJson != null) {
            response.put("toolResult", parseJson(toolResultJson));
        }
        return response;
    }

    private Map<String, Object> toolMetadata(String toolName) {
        ToolDefinition definition = registry.get(toolName);
        Map<String, Object> metadata = new LinkedHashMap<String, Object>();
        if (definition == null) {
            return metadata;
        }
        metadata.put("description", definition.getDescription());
        metadata.put("riskLevel", definition.getMetadata().getRiskLevel());
        metadata.put("auditLevel", definition.getMetadata().getAuditLevel());
        metadata.put("requiresPermission", definition.getMetadata().getRequiresPermission());
        metadata.put("parameters", parameterNames(definition));
        return metadata;
    }

    private Object parameterNames(ToolDefinition definition) {
        java.util.List<String> names = new java.util.ArrayList<String>();
        for (ToolParameter parameter : definition.getParameters()) {
            names.add(parameter.getName());
        }
        return names;
    }

    private Object parseJson(String value) {
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception ex) {
            return value;
        }
    }

    private static final class PlannedToolCall {
        private final String toolName;
        private final String argumentsJson;
        private final String approvalId;

        private PlannedToolCall(String toolName, String argumentsJson) {
            this(toolName, argumentsJson, null);
        }

        private PlannedToolCall(String toolName, String argumentsJson, String approvalId) {
            this.toolName = toolName;
            this.argumentsJson = argumentsJson;
            this.approvalId = approvalId;
        }

        public String getToolName() { return toolName; }
        public String getArgumentsJson() { return argumentsJson; }
        public String getApprovalId() { return approvalId; }
    }
}
