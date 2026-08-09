package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditQuery;
import com.c8software.spring.ai.core.audit.AuditRecord;
import com.c8software.spring.ai.core.context.ContextSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditLogQueryController {

    private final AuditLogger auditLogger;

    public AuditLogQueryController(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @GetMapping("/logs")
    public List<Map<String, Object>> logs(
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String callerUser,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return auditDtos(auditLogger.query(new AuditQuery(traceId, toolName, callerUser, tenantId, status, limit)));
    }

    @GetMapping("/conversation/{sessionId}")
    public List<Map<String, Object>> conversation(@PathVariable String sessionId) {
        List<AuditRecord> records = auditLogger.recent()
                .stream()
                .filter(record -> sessionId.equals(record.getTraceId()))
                .collect(Collectors.toList());
        return auditDtos(records);
    }

    private List<Map<String, Object>> auditDtos(List<AuditRecord> records) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AuditRecord record : records) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("traceId", record.getTraceId());
            dto.put("toolName", record.getToolName());
            dto.put("callerUser", record.getCallerUser());
            dto.put("tenantId", record.getTenantId());
            dto.put("inputHash", record.getInputHash());
            dto.put("outputHash", record.getOutputHash());
            dto.put("costMs", record.getCostMs());
            dto.put("status", record.getStatus());
            dto.put("errorMessage", record.getErrorMessage());
            dto.put("eventType", record.getEventType());
            dto.put("contextBeforeHash", record.getContextBeforeHash());
            dto.put("contextAfterHash", record.getContextAfterHash());
            dto.put("contextBefore", snapshotDto(record.getContextBefore()));
            dto.put("contextAfter", snapshotDto(record.getContextAfter()));
            dto.put("timestamp", record.getTimestamp() == null ? null : String.valueOf(record.getTimestamp()));
            result.add(dto);
        }
        return result;
    }

    private Map<String, Object> snapshotDto(ContextSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("sessionId", snapshot.getSessionId());
        dto.put("tenantId", snapshot.getTenantId());
        dto.put("userId", snapshot.getUserId());
        dto.put("role", snapshot.getRole());
        dto.put("modelProvider", snapshot.getModelProvider());
        dto.put("modelName", snapshot.getModelName());
        dto.put("taskId", snapshot.getTaskId());
        dto.put("taskType", snapshot.getTaskType());
        dto.put("taskStatus", snapshot.getTaskStatus() == null ? null : snapshot.getTaskStatus().name());
        dto.put("currentStep", snapshot.getCurrentStep());
        dto.put("pendingApproval", snapshot.isPendingApproval());
        dto.put("facts", snapshot.getFacts());
        dto.put("userOverrides", snapshot.getUserOverrides());
        dto.put("capturedAt", snapshot.getCapturedAt() == null ? null : String.valueOf(snapshot.getCapturedAt()));
        return dto;
    }
}
