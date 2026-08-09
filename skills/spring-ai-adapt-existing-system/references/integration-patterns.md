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

## Scanning Heuristics

Use names, annotations, and route paths together:

- `@Service` methods usually contain the best business capability candidates.
- `@Controller` and `@RestController` routes reveal user-facing operation names and permissions.
- DTO field names reveal parameter semantics and sensitive fields.
- Security annotations such as `@PreAuthorize` should be translated into `@AiToolRequiresPermission` metadata.
- Repository methods are inputs to facade design, not direct tool candidates.

Suggested name normalization:

- `queryUserBalance` -> `query_user_balance`
- `sendSms` -> `send_sms`
- `createOrder` -> `create_order`
- `refundOrder` -> `refund_order`
- `approveInvoice` -> `approve_invoice`

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

## Test Matrix

Every generated adaptation should include focused tests:

| Concern | Test expectation |
| --- | --- |
| Registration | Generated facade methods appear in `ToolRegistry`. |
| Schema | Parameters, required fields, enum values, descriptions, and validation bounds are exported. |
| Permission | Metadata contains the inferred or copied permission key. |
| Risk | `HIGH` and `CRITICAL` tools require approval before execution. |
| Sensitive data | Sensitive input/output is masked in audit-facing results. |
| Context | Confirmed user choices become replayable context facts. |
| Idempotency | Retried side-effecting calls return cached results when the same key is used. |
| Tenant isolation | Tenant A cannot see tenant B replay, feedback, or internal tools unless explicitly allowed. |
| MCP plan | Missing external capability returns a plan with id, risk, permissions, and approval requirement. |

## README Patch Template

````markdown
## AI Tool Adapter

This service exposes selected business capabilities as governed AI tools.

```xml
<dependency>
  <groupId>com.c8software.spring.ai</groupId>
  <artifactId>spring-ai-tool-adapter-starter</artifactId>
  <version>${spring-ai-tool-adapter.version}</version>
</dependency>
```

Inspect tools:

- `GET /api/tools`
- `GET /api/debug/schema`

High-risk tools use human approval before execution. Tool calls are audited with tenant, user, trace id, input/output hashes, and context snapshots.
````
