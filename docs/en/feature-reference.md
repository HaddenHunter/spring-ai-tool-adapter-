# Spring AI Tool Adapter Feature Reference

## 1. Overview

Spring AI Tool Adapter is an enterprise AI Tool Adapter for exposing Spring Bean business capabilities to LLMs and agents with governance, auditability, and structured context.

High-level flow:

```text
LLM / Agent
  -> Tool Schema
  -> Tool Adapter
  -> permission / masking / audit / context
  -> Spring Bean business system
```

Core areas:

1. annotations and governance metadata
2. tool registration and discovery
3. schema generation
4. execution engine
5. permission control
6. sensitive data masking
7. audit logging
8. session and context
9. task orchestration
10. multi-model schema adapters
11. semantic MCP provisioning
12. Codex skill for existing systems

## 2. Annotation System

### 2.1 Capability Annotation

`@AiTool` marks a Java method as AI-callable.

Fields:

- `name`: globally unique tool name
- `description`: business semantic description
- `paramDescriptions`: compatibility field for parameter descriptions
- `enabled`: static enable flag
- `requiresPermission`: legacy permission field
- `auditLevel`: legacy audit field

### 2.2 Governance Annotations

Governance annotations are the enterprise contract:

- `@AiToolRequiresPermission`
- `@AiToolRiskLevel`
- `@AiToolAudit`
- `@AiToolParam`
- `@AiToolSensitive`
- `@AiToolIdempotent`
- `@AiToolRollback`
- `@AiToolVisibility`
- `@AiToolVersion`
- `@AiToolContextKey`

These annotations are declarative metadata only. They do not contain business logic.

### 2.3 Metadata Enrichment

`AiToolRegistrar` and `ToolGovernanceAnnotationProcessor` transform annotations into:

- `ToolDefinition`
- `ToolMetadata`
- `ToolParameter`

`ToolMetadata` is immutable and includes:

- group
- permission
- audit level
- risk level
- visibility
- version
- idempotency configuration
- rollback configuration
- context binding
- timeout
- extra attributes

## 3. Tool Registration And Discovery

`AiToolRegistrar` is a Spring `BeanPostProcessor`. After Bean initialization, it scans methods annotated with `@AiTool`.

Registration flow:

```text
scan Bean
 -> find @AiTool methods
 -> parse governance annotations
 -> build ToolParameter
 -> build ToolMetadata
 -> build ReflectionToolDefinition
 -> register in ToolRegistry
```

Key features:

- globally unique tool names
- lookup by name
- lookup by group
- thread-safe registry
- MethodHandle cache
- Spring AOP / CGLIB target-class scanning

## 4. Schema Generation

`ToolSchemaConverter` converts a `ToolDefinition` into model-specific Tool Schema.

Built-in converters:

- `OpenAIFunctionSchemaConverter`
- `AzureOpenAISchemaConverter`
- `DeepSeekSchemaConverter`
- `TongyiQwenSchemaConverter`
- `DoubaoSchemaConverter`
- `OllamaSchemaConverter`

Supported types:

- `String`
- `Integer` / `Long`
- `BigDecimal` / `Double` / `Float`
- `Boolean`
- `Enum`
- simple `List<String>`

Validation export:

- `@NotNull`
- `@Min`
- `@Max`

## 5. Execution Engine

Core interface:

```java
public interface ToolExecutor {
    ToolResult execute(String toolName, String jsonArguments, ExecutionContext executionContext);
}
```

Default implementation:

```java
DefaultToolExecutor
```

Execution steps:

1. Lookup tool from `ToolRegistry`.
2. Deserialize JSON arguments.
3. Convert Java argument types.
4. Check permission.
5. Mask sensitive values.
6. Invoke business method through `MethodHandle`.
7. Produce `ToolResult`.
8. Write audit record.
9. Wrap exceptions.

## 6. Permission Control

SPI:

```java
PermissionChecker
```

Default implementation:

```java
DefaultPermissionChecker
```

Default behavior:

- Reads permission key from `ToolMetadata.requiresPermission`.
- Checks membership in `ExecutionContext.permissions`.
- Throws `AiToolSecurityException` when permission is missing.

Production integrations can replace it with Spring Security, RBAC, tenant scopes, data scopes, or expression-based permission checks.

## 7. Sensitive Data Masking

SPI:

```java
SensitiveMasker
```

Default implementation:

```java
DefaultSensitiveMasker
```

Sensitive types:

- `MOBILE`
- `ID_CARD`
- `BANK_CARD`
- `NAME`
- `PASSWORD`
- `OPERATOR_ID`
- `CUSTOM`

Current masking applies to audit input summaries. Return-value masking can be added through the same SPI boundary.

## 8. Audit Logging

SPI:

```java
AuditLogger
```

Default implementation:

```java
AsyncAuditLogger
```

Audit fields:

- `traceId`
- `toolName`
- `callerUser`
- `tenantId`
- `inputHash`
- `outputHash`
- `costMs`
- `status`
- `errorMessage`
- `eventType`
- `contextBeforeHash`
- `contextAfterHash`
- `timestamp`

Audit records support:

- who called which tool
- when it happened
- whether it succeeded
- input/output summary hashes
- context state before and after execution
- replay and compliance investigation

## 9. Session And Context

The context module answers:

- who owns the session
- what task is active
- where the task currently is
- which choices the user already confirmed
- whether state survives tool failure

Core types:

- `ConversationSession`
- `ConversationSessionStore`
- `TaskContext`
- `TaskStatus`
- `ContextFact`
- `UserChoiceTracker`
- `ContextCompressor`
- `ContextSnapshot`
- `ConversationContextHolder`

State model:

```text
Session: sessionId / tenantId / userId / role / model
Task: taskId / taskType / taskStatus / currentStep / pendingApproval
Choice: selectedCustomerId / selectedTemplateId / selectedAmount / userOverrides
```

Constraints:

- `adapter-core` does not depend on raw HTTP Session.
- Confirmed choices cannot be silently overwritten.
- Compression must preserve confirmed facts.
- Tool failure must not erase context.

## 10. Task Orchestration

The orchestration module provides a lightweight DAG model:

- `TaskDefinition`
- `TaskNode`
- `TaskEdge`
- `TaskNodeType`
- `TaskExecutor`
- `HumanInTheLoop`
- `ApprovalRequest`
- `ApprovalResponse`

Node types:

- `TOOL`
- `CONDITION`
- `HUMAN_APPROVAL`
- `SUB_TASK`

The current implementation focuses on base models and tool-node execution. Condition branches, approval waiting, rollback, and long-running tasks can be expanded through the existing abstractions.

## 11. Semantic MCP Provisioning

This module turns natural-language integration needs into an MCP provisioning plan.

Core types:

- `McpCapabilityCatalog`
- `McpCapabilityDescriptor`
- `McpSemanticMatcher`
- `McpProvisioningPlanner`
- `McpProvisionPlan`

Default catalog entries:

- CRM Customer MCP
- Finance Readonly MCP
- Messaging MCP

Possible statuses:

- `NO_MATCH`
- `PERMISSION_REQUIRED`
- `PENDING_APPROVAL`
- `READY_TO_PROVISION`

Boundaries:

- no automatic MCP installation
- no automatic external system authorization
- plan generation only
- enterprise code can replace catalog and matcher

## 12. Demo Features

Demo tools:

- `mock_query_user_balance`
- `mock_send_sms`
- `mock_create_order`
- `mock_query_weather`
- `mock_query_complaint_customer`

Demo UI includes:

- conversation area
- tool-call cards
- task visualization
- governance panel
- debug panel
- prompt / schema / audit views

## 13. Codex Skill

Built-in skill:

```text
skills/spring-ai-adapt-existing-system
```

It helps adopters transform an existing Spring system into a governed AI Tool system.

Generated outputs:

- tool facades
- governance annotations
- context keys
- MCP catalog
- tests

## 14. Current Boundaries

The current version focuses on v0.x Tool Adapter capabilities:

- Core annotations, registration, schema, execution, audit, context, MCP planning, and demo are provided.
- Production-grade durable audit storage is not included.
- A real approval workflow is not included.
- A real MCP installer is not included.
- A complete long-task runtime is not included.

These concerns are intentionally left behind SPIs for enterprise implementations.
