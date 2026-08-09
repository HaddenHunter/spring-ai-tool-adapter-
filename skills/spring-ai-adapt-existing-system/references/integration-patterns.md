# Integration Patterns

## Tool Selection

Expose business capabilities, not every Java method.

Good candidates:

- `queryUserBalance`
- `sendSms`
- `createOrder`
- `refundOrder`
- `approveInvoice`
- `queryCustomerProfile`

Poor candidates:

- generic `save`
- generic `deleteById`
- repository methods
- methods returning secrets, tokens, passwords, or raw credentials
- internal infrastructure methods

## Permission Keys

Use stable domain-action keys:

- Read: `domain:read`
- Create: `domain:create`
- Update: `domain:update`
- Delete: `domain:delete`
- Approval: `domain:approve`
- Refund/payment: `order:refund`, `payment:create`, `finance:read`
- Admin configuration: `admin:config`

## Risk Levels

- `LOW`: read-only lookup, non-sensitive query
- `MEDIUM`: notification, customer contact, non-critical workflow step
- `HIGH`: money movement, refunds, order creation, data mutation, customer deletion
- `CRITICAL`: system configuration, permissions, tenant settings, bulk export

## Audit Levels

- `BASIC`: low-risk operational lookup
- `FULL`: sensitive data, side effects, customer contact, money movement, approval actions
- `NONE`: only for explicitly non-business internal helper tools; avoid for enterprise tools

## Context Keys

Use context keys for facts the conversation should remember:

- `selectedCustomerId`
- `selectedOrderId`
- `selectedTemplateId`
- `selectedAmount`
- `confirmedApprovalId`
- `lastRefundOrderId`
- `lastCreatedOrderId`

Confirmed user choices should use `confirmed = true`.

## Idempotency

Use `@AiToolIdempotent` for any tool that can be retried by the LLM or network layer.

Preferred keys:

- existing business idempotency key
- order id
- request id
- natural unique tuple such as `#customerId + ':' + #templateId`

## Rollback

Only add rollback metadata if a real compensating method exists.

Examples:

- create order -> cancel order
- create refund -> cancel refund
- send notification -> mark notification invalid

## Semantic MCP Provisioning

Generate MCP provisioning code when the target system lacks a needed external integration.

Rules:

- Produce a `McpCapabilityCatalog` bean or extend an existing catalog.
- Use semantic tags for user language and domain terms.
- Return `McpProvisionPlan` from application services or controllers.
- Never auto-install external MCP servers from the skill output.
- Require permission and approval for external systems.

Example catalog bean:

```java
@Bean
public McpCapabilityCatalog customerMcpCatalog() {
    return () -> Collections.singletonList(new McpCapabilityDescriptor(
            "mcp.customer.crm",
            "Customer CRM MCP",
            "Connect customer profile and ticket data.",
            Arrays.asList("customer", "crm", "ticket"),
            Collections.singletonList("mcp:provision:crm"),
            McpRiskLevel.MEDIUM,
            true
    ));
}
```
