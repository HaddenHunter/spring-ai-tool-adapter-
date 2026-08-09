package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditQuery;
import com.c8software.spring.ai.core.audit.AuditRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditLogQueryController {

    private final AuditLogger auditLogger;

    public AuditLogQueryController(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @GetMapping("/logs")
    public List<AuditRecord> logs(
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String callerUser,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit
    ) {
        return auditLogger.query(new AuditQuery(traceId, toolName, callerUser, tenantId, status, limit));
    }

    @GetMapping("/conversation/{sessionId}")
    public List<AuditRecord> conversation(@PathVariable String sessionId) {
        return auditLogger.recent()
                .stream()
                .filter(record -> sessionId.equals(record.getTraceId()))
                .collect(Collectors.toList());
    }
}
