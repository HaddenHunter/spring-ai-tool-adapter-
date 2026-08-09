---
name: spring-ai-adapt-existing-system
description: Generate Spring AI Tool Adapter integration code for an existing Spring Boot or Spring MVC system. Use when asked to scan existing services/controllers/repositories and automatically add AI-callable tools, governance annotations, context bindings, MCP semantic provisioning plans, adapter configuration, and tests for the com.c8software.spring.ai framework.
---

# Spring AI Adapt Existing System

## Purpose

Use this skill to retrofit an existing Spring application with Spring AI Tool Adapter support. The output should be code changes in the target system, not a generic proposal.

The skill turns existing business methods into governed AI tools:

- `@AiTool` for capability exposure
- governance annotations for permission, risk, audit, sensitive parameters, idempotency, rollback, visibility, version, and context keys
- session/context binding for multi-turn tasks
- optional semantic MCP provisioning plans for integrations that are not already present
- tests proving registration, schema export, permission metadata, and execution behavior

## Workflow

1. Inspect the target project before editing.
   - Find `pom.xml` or `build.gradle`.
   - Find Spring Boot entry points, `@Service`, `@Component`, `@Controller`, and security configuration.
   - Prefer `rg` for discovery.
   - Build a candidate inventory before editing. Include class name, method name, parameters, return type, inferred domain, inferred risk, and why it should or should not become a tool.

2. Classify candidate business capabilities.
   - Query/read methods are usually `RiskLevel.LOW`.
   - Notification methods are usually `RiskLevel.MEDIUM`.
   - Create/update/delete/refund/payment/config methods are usually `RiskLevel.HIGH` or `CRITICAL`.
   - Do not expose raw CRUD or internal infrastructure methods unless they map to a business capability.

3. Generate adapter dependency and configuration.
   - Add the project dependency for `com.c8software.spring.ai`.
   - Enable auto-configuration only if the target project does not already load it.
   - Do not add HTTP session coupling to adapter-core.

4. Annotate existing methods or create thin adapter facade methods.
   - Prefer a facade class named like `AiBusinessTools` when existing service methods are too low-level.
   - Keep business logic in existing services.
   - The AI tool method should delegate to existing services and only carry semantic/governance annotations.
   - Generate one facade per bounded domain when a project has many services, for example `CustomerAiTools`, `FinanceAiTools`, or `OrderAiTools`.

5. Add governance annotations.
   - Always add `@AiToolRequiresPermission` for enterprise capabilities.
   - Always add `@AiToolRiskLevel`.
   - Always add `@AiToolAudit`.
   - Add `@AiToolSensitive` to sensitive arguments.
   - Add `@AiToolIdempotent` for side-effecting methods that can be retried.
   - Add `@AiToolRollback` only when a real compensating method exists.
   - Add `@AiToolContextKey` when parameters or results should persist as structured task facts.

6. Add semantic MCP provisioning only when the natural language request mentions missing integrations.
   - Create or extend an `McpCapabilityCatalog` bean in the target app.
   - Return an `McpProvisionPlan`; do not auto-install or auto-enable external MCP servers.
   - High-risk or external-data MCP capabilities must be approval-gated.
   - Add semantic tags from domain names, controller route names, DTO names, and common user phrases.
   - Add tests for the matched capability id, required permissions, risk level, and approval requirement.

7. Add tests.
   - Test tool registration.
   - Test metadata enrichment.
   - Test schema export for parameters.
   - Test execution path with mocked service dependencies.
   - Test MCP provisioning plans when MCP catalog or matcher code is generated.
   - Test that high-risk tools require approval instead of executing directly.
   - Test that sensitive parameters or return values are masked in audit-facing results.
   - Test tenant A/B visibility or replay isolation when the target project has tenant context.

8. Validate.
   - Run the target build (`mvn test` or Gradle equivalent).
   - Scan generated Java for placeholder packages and encoding damage.
   - Report exact files changed and verification results.

## Required Artifacts

When adapting an existing system, produce these artifacts whenever the target project shape allows it:

- `docs/ai-tool-adapter-candidates.md`: a table of scanned services and recommended tool mappings.
- One or more `*AiTools` facade classes under the target project's real package.
- Adapter configuration in `application.yml` or Java config only when needed.
- Registration/schema/execution tests.
- Optional `McpCapabilityCatalog` extension when external integrations are requested.
- A README patch showing the one-minute usage path for that target system.

## Candidate Inventory Format

Use this table shape in `docs/ai-tool-adapter-candidates.md`:

| Class | Method | Tool Name | Risk | Permission | Context Keys | Decision |
| --- | --- | --- | --- | --- | --- | --- |
| `OrderService` | `createOrder` | `order_create` | `HIGH` | `order:create` | `selectedCustomerId,lastOrderId` | expose via facade |
| `UserRepository` | `save` | - | - | - | - | reject: raw persistence method |

## Generated Facade Rules

- Facades must not contain business logic beyond parameter normalization and delegation.
- Tool names must be stable snake_case business capabilities.
- Do not expose methods returning credentials, secrets, raw tokens, or unrestricted exports.
- Add `@AiToolVisibility(ToolVisibility.INTERNAL)` for orchestration-only tools.
- Add `@AiToolVersion` when changing an existing tool contract.
- Add `@AiToolContextKey` on parameters or methods when user choices should survive future turns.

## README Patch Rules

Add a short target-system section with:

- dependency snippet
- one example annotated facade method
- endpoint or UI path to inspect registered tools
- how high-risk approval is handled
- how to run tests

## Generation Rules

- Use package names from the target project. Never use placeholder package names.
- Use `com.c8software.spring.ai` imports for this framework.
- Keep adapter facades small and deterministic.
- Do not invent business permissions silently; infer reasonable keys and mark assumptions in the final response.
- Do not put permission, approval, idempotency storage, or MCP installation logic inside annotations.
- Do not expose methods that can leak secrets, credentials, raw tokens, or unrestricted exports.
- Do not expose high-risk actions without `@AiToolRiskLevel(HIGH)` or `CRITICAL` and an approval path.

## Output Pattern

For each generated tool, ensure the code has this shape:

```java
@AiTool(name = "refund_order", description = "Create a refund for an order")
@AiToolRequiresPermission("order:refund")
@AiToolRiskLevel(RiskLevel.HIGH)
@AiToolAudit(AuditLevel.FULL)
@AiToolIdempotent(key = "#orderId")
@AiToolContextKey(store = "lastRefundOrderId")
public RefundResult refundOrder(
        @AiToolParam(description = "order id") Long orderId,
        @AiToolParam(description = "refund amount") BigDecimal amount,
        @AiToolSensitive(type = SensitiveType.OPERATOR_ID) Long operatorId
) {
    return refundService.refund(orderId, amount, operatorId);
}
```

## Reference

Read `references/integration-patterns.md` when you need concrete mapping rules for permissions, risk levels, context keys, and MCP catalogs.
