# Spring AI Tool Adapter User Guide

## 1. Purpose

Spring AI Tool Adapter is an enterprise-oriented Java / Spring Boot framework for AI Tool Calling. It does more than expose Java methods to an LLM. It declares business intent, permissions, risk, audit level, masking policy, conversation context, and MCP integration boundaries.

Core principles:

- A tool is a business capability, not a generic function.
- Every tool call must be observable and auditable.
- High-risk actions must support human-in-the-loop approval.
- Multi-turn context is structured business state, not raw chat history.
- The framework provides contracts, SPIs, and defaults; business logic remains in the adopter's system.

## 2. Modules

```text
spring-ai-tool-adapter
├── adapter-core   Core framework
├── adapter-demo   Runnable demo and Chat UI
├── skills         Codex skill for adapting existing Spring systems
├── docs           English and Chinese manuals
└── *.yaml / *.md  Architecture, boundaries, roadmap, and phase prompts
```

`adapter-core` provides:

- annotations and governance metadata
- tool registration and discovery
- schema conversion
- execution engine
- permission, masking, and audit SPIs
- session and context state
- task orchestration models
- semantic MCP provisioning SPIs

`adapter-demo` provides:

- mock tools
- Chat UI
- tool listing endpoint
- schema / prompt / audit debug endpoints
- MCP semantic planning endpoint

## 3. Requirements

- JDK 8 or JDK 17
- Maven 3.8+
- Spring Boot 2.x or 3.x style applications can follow the integration pattern

## 4. Quick Start

Run tests:

```bash
mvn test
```

Start the demo:

```bash
mvn -pl adapter-demo spring-boot:run
```

Open:

```text
http://localhost:8080/chat
```

Useful demo endpoints:

```text
GET  /api/tools
GET  /api/debug/schema
GET  /api/debug/prompt
GET  /api/governance
GET  /api/audit/logs
POST /api/chat
POST /api/mcp/semantic-plan
```

## 5. Integrating An Existing Spring System

### 5.1 Add Dependency

If the framework is published to your internal Maven repository, add the starter:

```xml
<dependency>
    <groupId>com.c8software.spring.ai</groupId>
    <artifactId>spring-ai-tool-adapter-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

The starter pulls in `adapter-core` and registers auto-configuration. For source-based development, you can also depend on `adapter-core` directly.

### 5.2 Enable Auto Configuration

The starter loads the default auto-configuration:

```java
com.c8software.spring.ai.core.config.AiToolAutoConfiguration
```

If the application does not use the starter, or auto-configuration is not active, import it explicitly:

```java
@Import(AiToolAutoConfiguration.class)
```

### 5.3 Declare An AI Tool

Prefer a thin facade that delegates to existing services:

```java
@Component
@ToolGroup("order")
public class OrderAiTools {

    private final OrderService orderService;

    public OrderAiTools(OrderService orderService) {
        this.orderService = orderService;
    }

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
        return orderService.refund(orderId, amount, operatorId);
    }
}
```

Guidelines:

- Keep business logic inside existing services.
- Do not expose raw repository CRUD methods directly to the LLM.
- High-risk actions must declare risk, permission, audit level, and idempotency.
- Annotation processors must not contain business policy logic.

## 6. Governance Annotations

| Annotation | Purpose |
| --- | --- |
| `@AiTool` | Marks a method as AI-callable |
| `@AiToolRequiresPermission` | Declares required permission |
| `@AiToolRiskLevel` | Declares business risk |
| `@AiToolAudit` | Declares audit level |
| `@AiToolParam` | Describes parameter semantics |
| `@AiToolSensitive` | Marks sensitive parameters |
| `@AiToolIdempotent` | Declares idempotency key |
| `@AiToolRollback` | Declares rollback method |
| `@AiToolVisibility` | Declares visibility |
| `@AiToolVersion` | Declares tool contract version |
| `@AiToolContextKey` | Binds a parameter or result to conversation context |

Risk levels:

```java
LOW
MEDIUM
HIGH
CRITICAL
```

Audit levels:

```java
NONE
BASIC
FULL
```

Visibility:

```java
PUBLIC
INTERNAL
DEPRECATED
```

## 7. Executing Tools

Core API:

```java
ToolResult execute(String toolName, String jsonArguments, ExecutionContext executionContext);
```

Example:

```java
ExecutionContext context = new ExecutionContext(
        "user-1",
        "tenant-1",
        "trace-1",
        Collections.singleton("order:refund"),
        Instant.now()
);

ToolResult result = toolExecutor.execute(
        "refund_order",
        "{\"orderId\":1001,\"amount\":20.5,\"operatorId\":9}",
        context
);
```

Execution flow:

```text
JSON arguments
 -> argument binding
 -> permission check
 -> high-risk approval
 -> idempotency hit check
 -> sensitive masking
 -> MethodHandle business invocation
 -> idempotency result storage
 -> audit record
 -> ToolResult
```

### 7.1 Human-in-the-loop Approval

`HIGH` and `CRITICAL` risk tools trigger `ToolApprovalManager`.

The default auto-configuration is safe by default: if no real `HumanInTheLoop` implementation is provided, high-risk tools are rejected with an approval-required status instead of being executed silently.

Applications can provide their own Bean:

```java
@Bean
public HumanInTheLoop humanInTheLoop() {
    return request -> approvalService.requestApproval(request);
}
```

### 7.2 Idempotency Protection

Tools annotated with `@AiToolIdempotent` use `IdempotencyStore` to cache successful results. When a repeated call uses the same key, the framework returns the previous result instead of invoking the business method again.

The default store is in-memory. Production deployments should replace it with Redis or a database.

## 8. Tool Schema Generation

Built-in schema converters:

- OpenAI
- Azure OpenAI
- DeepSeek
- Tongyi Qwen
- Doubao
- Ollama

Example:

```java
ToolSchemaConverter converter = new OpenAIFunctionSchemaConverter();
Map<String, Object> schema = converter.convert(toolDefinition);
```

Exported schema features:

- primitive Java types to JSON Schema
- enums
- required parameters
- `@Min` / `@Max`
- parameter descriptions

## 9. Session And Context

Context is structured business state, not chat history.

Core types:

- `ConversationSession`
- `TaskContext`
- `ContextFact`
- `UserChoiceTracker`
- `ContextCompressor`
- `ContextSnapshot`

Persist a confirmed user choice:

```java
userChoiceTracker.confirmChoice(
        taskContext,
        "selectedCustomerId",
        "1001",
        "user-confirmed"
);
```

Bind context around tool execution:

```java
ConversationContextHolder.bind(session, taskContext);
try {
    toolExecutor.execute(toolName, arguments, executionContext);
} finally {
    ConversationContextHolder.clear();
}
```

Audit records include before/after context snapshot hashes for replay.

## 10. Semantic MCP Provisioning

When a user asks to connect an external system, the framework can turn natural language into an MCP provisioning plan.

Core APIs:

- `McpCapabilityCatalog`
- `McpSemanticMatcher`
- `McpProvisioningPlanner`

Example:

```java
McpProvisionPlan plan = planner.plan(new McpSemanticRequest(
        "session-1",
        "tenant-1",
        "user-1",
        "connect customer CRM and ticket data",
        Collections.singletonList("mcp:provision:crm")
));
```

Boundaries:

- The framework returns a provisioning plan only.
- It does not install external MCP servers automatically.
- It does not enable external system access automatically.
- High-risk MCP capabilities must require approval.
- Missing permission must produce `PERMISSION_REQUIRED`.

## 11. Codex Skill For Existing Systems

The repository includes:

```text
skills/spring-ai-adapt-existing-system
```

This skill guides Codex to scan an existing Spring project and generate:

- tool facades
- governance annotations
- context bindings
- MCP provisioning catalog
- unit tests

Typical prompt:

```text
Use $spring-ai-adapt-existing-system to analyze this Spring project and generate governed Spring AI Tool Adapter integration code.
```

## 12. Extension Points

| SPI | Purpose |
| --- | --- |
| `PermissionChecker` | Integrate enterprise authorization |
| `SensitiveMasker` | Customize masking rules |
| `AuditLogger` | Store audit records in databases or log systems |
| `ToolExecutor` | Replace execution behavior |
| `ToolRegistry` | Provide custom tool sources |
| `ToolSchemaConverter` | Support new model schema formats |
| `ContextCompressor` | Customize context compression |
| `UserChoiceTracker` | Customize user choice persistence |
| `McpCapabilityCatalog` | Provide an enterprise MCP capability catalog |
| `McpSemanticMatcher` | Replace semantic matching logic |
| `ToolApprovalManager` | Customize high-risk approval policy |
| `HumanInTheLoop` | Integrate enterprise approval systems |
| `IdempotencyStore` | Integrate Redis / DB idempotency storage |
| `ToolVisibilityFilter` | Control whether tools are visible to the LLM |

## 13. Persistent Audit Logging

Without a database, the framework uses `AsyncAuditLogger`. When a Spring `DataSource` exists, auto-configuration enables `JdbcAuditLogger` and creates the `ai_tool_audit_log` table.

Query endpoint:

```text
GET /api/audit/logs?traceId=...&toolName=...&callerUser=...&tenantId=...&status=...&limit=100
```

## 14. Production Recommendations

- Store audit logs in durable infrastructure instead of memory.
- Integrate permission checks with Spring Security, RBAC, tenants, and data scopes.
- Put high-risk tools behind approval workflows.
- Store idempotency keys in Redis, a database, or another shared store.
- Treat MCP provisioning as a plan; installation and authorization should remain admin-approved.
- Run sensitivity and authorization tests before exposing tools to production users.
