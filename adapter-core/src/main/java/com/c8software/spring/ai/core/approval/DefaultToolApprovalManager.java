package com.c8software.spring.ai.core.approval;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.orchestration.ApprovalRequest;
import com.c8software.spring.ai.core.orchestration.ApprovalResponse;
import com.c8software.spring.ai.core.orchestration.HumanInTheLoop;

import java.util.Locale;
import java.util.UUID;

public class DefaultToolApprovalManager implements ToolApprovalManager {

    private final HumanInTheLoop humanInTheLoop;

    public DefaultToolApprovalManager(HumanInTheLoop humanInTheLoop) {
        this.humanInTheLoop = humanInTheLoop;
    }

    public ApprovalDecision approve(ToolDefinition definition, ExecutionContext context, String maskedInput) {
        if (!requiresApproval(definition)) {
            return ApprovalDecision.approved("approval not required");
        }
        ApprovalRequest request = new ApprovalRequest();
        request.setId(UUID.randomUUID().toString());
        request.setTitle("Approve AI tool call: " + definition.getName());
        request.setDetail("risk=" + definition.getMetadata().getRiskLevel()
                + ", user=" + (context == null ? "" : context.getCurrentUser())
                + ", tenant=" + (context == null ? "" : context.getTenantId())
                + ", input=" + maskedInput);
        request.setTimeoutMillis(30L * 60L * 1000L);
        ApprovalResponse response = humanInTheLoop.requestApproval(request);
        if (response != null && response.isApproved()) {
            return ApprovalDecision.approved(response.getComment());
        }
        return ApprovalDecision.rejected(response == null ? "REJECTED" : response.getStatus(),
                response == null ? "approval rejected" : response.getComment());
    }

    private boolean requiresApproval(ToolDefinition definition) {
        String risk = definition.getMetadata().getRiskLevel();
        if (risk == null) {
            return false;
        }
        String normalized = risk.toUpperCase(Locale.ENGLISH);
        return "HIGH".equals(normalized) || "CRITICAL".equals(normalized);
    }
}
