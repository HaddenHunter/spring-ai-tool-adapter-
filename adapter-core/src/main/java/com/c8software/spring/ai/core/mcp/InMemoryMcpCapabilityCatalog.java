package com.c8software.spring.ai.core.mcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InMemoryMcpCapabilityCatalog implements McpCapabilityCatalog {

    private final List<McpCapabilityDescriptor> capabilities = new ArrayList<McpCapabilityDescriptor>();

    public InMemoryMcpCapabilityCatalog() {
        capabilities.add(new McpCapabilityDescriptor(
                "mcp.crm.customer",
                "CRM Customer MCP",
                "Connect customer profile, ticket, and sales records.",
                Arrays.asList("customer", "crm", "client", "ticket", "sales", "\u5ba2\u6237", "\u5de5\u5355", "\u9500\u552e"),
                Collections.singletonList("mcp:provision:crm"),
                McpRiskLevel.MEDIUM,
                true
        ));
        capabilities.add(new McpCapabilityDescriptor(
                "mcp.finance.readonly",
                "Finance Readonly MCP",
                "Connect invoices, balance, payment status, and finance reports.",
                Arrays.asList("finance", "invoice", "payment", "balance", "report", "\u8d22\u52a1", "\u53d1\u7968", "\u4ed8\u6b3e", "\u4f59\u989d"),
                Collections.singletonList("mcp:provision:finance"),
                McpRiskLevel.HIGH,
                true
        ));
        capabilities.add(new McpCapabilityDescriptor(
                "mcp.messaging.notify",
                "Messaging MCP",
                "Connect SMS, email, and enterprise notification channels.",
                Arrays.asList("sms", "message", "email", "notify", "notification", "\u77ed\u4fe1", "\u90ae\u4ef6", "\u901a\u77e5"),
                Collections.singletonList("mcp:provision:messaging"),
                McpRiskLevel.MEDIUM,
                true
        ));
    }

    public List<McpCapabilityDescriptor> list() {
        return Collections.unmodifiableList(capabilities);
    }
}
