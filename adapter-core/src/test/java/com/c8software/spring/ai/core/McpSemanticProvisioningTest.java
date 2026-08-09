package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.mcp.DefaultMcpProvisioningPlanner;
import com.c8software.spring.ai.core.mcp.DefaultMcpSemanticMatcher;
import com.c8software.spring.ai.core.mcp.InMemoryMcpCapabilityCatalog;
import com.c8software.spring.ai.core.mcp.McpProvisionPlan;
import com.c8software.spring.ai.core.mcp.McpSemanticRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class McpSemanticProvisioningTest {

    @Test
    void plansMcpFromChineseSemanticRequestWithApprovalGate() {
        DefaultMcpProvisioningPlanner planner = new DefaultMcpProvisioningPlanner(
                new InMemoryMcpCapabilityCatalog(),
                new DefaultMcpSemanticMatcher()
        );

        McpProvisionPlan plan = planner.plan(new McpSemanticRequest(
                "session-1",
                "tenant-1",
                "user-1",
                "\u5e2e\u6211\u63a5\u5165\u5ba2\u6237CRM\u548c\u5de5\u5355\u7cfb\u7edf",
                Collections.singletonList("mcp:provision:crm")
        ));

        assertThat(plan.getStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(plan.isAllowedByPermission()).isTrue();
        assertThat(plan.isApprovalRequired()).isTrue();
        assertThat(plan.getMatch().getCapability().getId()).isEqualTo("mcp.crm.customer");
    }

    @Test
    void blocksPlanWhenPermissionIsMissing() {
        DefaultMcpProvisioningPlanner planner = new DefaultMcpProvisioningPlanner(
                new InMemoryMcpCapabilityCatalog(),
                new DefaultMcpSemanticMatcher()
        );

        McpProvisionPlan plan = planner.plan(new McpSemanticRequest(
                "session-1",
                "tenant-1",
                "user-1",
                "connect invoice and finance reports",
                Arrays.asList("mcp:provision:crm")
        ));

        assertThat(plan.getStatus()).isEqualTo("PERMISSION_REQUIRED");
        assertThat(plan.isAllowedByPermission()).isFalse();
        assertThat(plan.getMatch().getCapability().getId()).isEqualTo("mcp.finance.readonly");
    }
}
