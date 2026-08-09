package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.audit.AuditLogger;
import com.c8software.spring.ai.core.audit.AuditRecord;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<AuditRecord> logs() {
        return auditLogger.recent();
    }

    @GetMapping("/conversation/{sessionId}")
    public List<AuditRecord> conversation(@PathVariable String sessionId) {
        return auditLogger.recent()
                .stream()
                .filter(record -> sessionId.equals(record.getTraceId()))
                .collect(Collectors.toList());
    }
}
