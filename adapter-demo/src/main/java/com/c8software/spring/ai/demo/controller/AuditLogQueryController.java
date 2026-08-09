package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditQuery;
import com.c8software.spring.ai.core.audit.AuditRecord;
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
            dto.put("timestamp", record.getTimestamp() == null ? null : String.valueOf(record.getTimestamp()));
            result.add(dto);
        }
        return result;
    }
}
