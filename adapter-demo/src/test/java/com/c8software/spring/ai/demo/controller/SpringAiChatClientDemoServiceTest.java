package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.approval.DefaultToolApprovalManager;
import com.c8software.spring.ai.core.audit.AsyncAuditLogger;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.idempotency.DefaultIdempotencyKeyResolver;
import com.c8software.spring.ai.core.idempotency.InMemoryIdempotencyStore;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.DefaultPermissionChecker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.c8software.spring.ai.demo.tool.DemoTools;
import com.c8software.spring.ai.springai.GovernedToolCallbackProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringAiChatClientDemoServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void approvalResumeExecutesHighRiskToolThroughGovernedCallback() {
        DemoApprovalStore approvals = new DemoApprovalStore();
        SpringAiChatClientDemoService service = service(approvals);

        Map<String, Object> pending = service.chat("session-1", "Create an order for customer 1001");
        assertEquals("PENDING_APPROVAL", pending.get("status"));

        String approvalId = String.valueOf(((Map<?, ?>) pending.get("approval")).get("approvalId"));
        Map<String, Object> approved = service.approve(approvalId);

        assertEquals("APPROVED_AND_EXECUTED", approved.get("status"));
        assertEquals("APPROVED", ((Map<?, ?>) approved.get("approval")).get("status"));
        assertTrue(Boolean.TRUE.equals(((Map<?, ?>) approved.get("toolResult")).get("success")));
    }

    @Test
    void rejectionStopsHighRiskToolBeforeResume() {
        DemoApprovalStore approvals = new DemoApprovalStore();
        SpringAiChatClientDemoService service = service(approvals);

        Map<String, Object> pending = service.chat("session-1", "Create an order for customer 1001");
        String approvalId = String.valueOf(((Map<?, ?>) pending.get("approval")).get("approvalId"));

        Map<String, Object> rejected = service.reject(approvalId);

        assertEquals("REJECTED", rejected.get("status"));
        assertEquals("REJECTED", ((Map<?, ?>) rejected.get("approval")).get("status"));
        assertFalse(rejected.containsKey("toolResult"));
    }

    private SpringAiChatClientDemoService service(DemoApprovalStore approvals) {
        ToolRegistry registry = new ToolRegistry();
        AiToolRegistrar registrar = new AiToolRegistrar(registry, new AiToolProperties());
        registrar.postProcessAfterInitialization(new DemoTools(), "demoTools");

        ToolExecutor executor = new DefaultToolExecutor(registry, new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(), new AsyncAuditLogger(), objectMapper, new AiToolProperties(),
                new DefaultToolApprovalManager(new DemoHumanInTheLoopConfiguration().demoHumanInTheLoop(approvals)),
                new InMemoryIdempotencyStore(), new DefaultIdempotencyKeyResolver());
        ToolCallbackProvider callbacks = new GovernedToolCallbackProvider(registry, executor);
        return new SpringAiChatClientDemoService(registry, callbacks, approvals, objectMapper);
    }
}
