package com.c8software.spring.ai.demo.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class DemoApprovalStore {

    private final ConcurrentMap<String, PendingApproval> pending = new ConcurrentHashMap<String, PendingApproval>();

    private final ConcurrentMap<String, Boolean> approvedTools = new ConcurrentHashMap<String, Boolean>();

    public PendingApproval create(String sessionId, String utterance, String toolName, String argumentsJson,
                                  String riskLevel, String reason) {
        PendingApproval approval = new PendingApproval(UUID.randomUUID().toString(), sessionId, utterance,
                toolName, argumentsJson, riskLevel, reason, "PENDING", Instant.now());
        pending.put(approval.getApprovalId(), approval);
        return approval;
    }

    public PendingApproval get(String approvalId) {
        return pending.get(approvalId);
    }

    public List<PendingApproval> list() {
        List<PendingApproval> result = new ArrayList<PendingApproval>(pending.values());
        Collections.sort(result);
        return result;
    }

    public void markApproved(String approvalId) {
        PendingApproval approval = pending.get(approvalId);
        if (approval != null) {
            approval.setStatus("APPROVED");
            approvedTools.put(approval.getToolName(), Boolean.TRUE);
        }
    }

    public void markRejected(String approvalId) {
        PendingApproval approval = pending.get(approvalId);
        if (approval != null) {
            approval.setStatus("REJECTED");
        }
    }

    public boolean consumeToolApproval(String toolName) {
        return approvedTools.remove(toolName) != null;
    }

    public Map<String, Object> toDto(PendingApproval approval) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();
        dto.put("approvalId", approval.getApprovalId());
        dto.put("sessionId", approval.getSessionId());
        dto.put("utterance", approval.getUtterance());
        dto.put("toolName", approval.getToolName());
        dto.put("argumentsJson", approval.getArgumentsJson());
        dto.put("riskLevel", approval.getRiskLevel());
        dto.put("reason", approval.getReason());
        dto.put("status", approval.getStatus());
        dto.put("createdAt", approval.getCreatedAt().toString());
        return dto;
    }

    public static final class PendingApproval implements Comparable<PendingApproval> {
        private final String approvalId;
        private final String sessionId;
        private final String utterance;
        private final String toolName;
        private final String argumentsJson;
        private final String riskLevel;
        private final String reason;
        private final Instant createdAt;
        private volatile String status;

        PendingApproval(String approvalId, String sessionId, String utterance, String toolName,
                        String argumentsJson, String riskLevel, String reason, String status, Instant createdAt) {
            this.approvalId = approvalId;
            this.sessionId = sessionId;
            this.utterance = utterance;
            this.toolName = toolName;
            this.argumentsJson = argumentsJson;
            this.riskLevel = riskLevel;
            this.reason = reason;
            this.status = status;
            this.createdAt = createdAt;
        }

        public String getApprovalId() { return approvalId; }
        public String getSessionId() { return sessionId; }
        public String getUtterance() { return utterance; }
        public String getToolName() { return toolName; }
        public String getArgumentsJson() { return argumentsJson; }
        public String getRiskLevel() { return riskLevel; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
        public Instant getCreatedAt() { return createdAt; }

        void setStatus(String status) {
            this.status = status;
        }

        public int compareTo(PendingApproval other) {
            return other.createdAt.compareTo(createdAt);
        }
    }
}
