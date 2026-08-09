# Spring AI Tool Adapter Launch Kit

## One-Line Positioning

Spring AI Tool Adapter is an enterprise-grade Java framework that exposes existing Spring Bean capabilities to LLMs and Agents with governance, audit, approval, masking, context, and recovery.

## Core Message

We do not just expose methods to LLMs. We annotate business intent, risk, and trust boundaries.

## Who It Is For

- Spring Boot teams adding AI tool calling to existing business systems
- Enterprise teams that need permissions, audit, approvals, masking, idempotency, and tenant boundaries
- Java teams evaluating Spring AI, LangChain4j, MCP, and Agent workflows
- Teams that want Codex to retrofit existing Spring services into governed AI tools

## Key Differentiators

1. Enterprise Tool Governance
   - `@AiTool` for capability exposure
   - `@AiToolRiskLevel` for business risk
   - `@AiToolRequiresPermission` for authorization metadata
   - `@AiToolSensitive` for sensitive data boundaries
   - `@AiToolIdempotent` for retry safety
   - Human approval loop for high-risk tools

2. Audit As Evidence
   - traceId, tenantId, userId, toolName, status, and cost
   - input/output hashes
   - context snapshots before and after execution
   - audit UI with filtering and context comparison

3. Session + Context
   - Context is structured business state, not raw chat history
   - Confirmed user choices become context facts
   - Multi-turn task state supports replay and recovery

4. Spring AI Native Bridge
   - Exposes governed tools through Spring AI `ToolCallbackProvider`
   - Keeps permissions, audit, masking, approval, and timeout isolation inside the adapter
   - Includes a ChatClient-style approval demo

5. Java-Native Agent Harness
   - YAML/JSON Flow Spec
   - Phase / Step / Artifact / Checkpoint
   - Human nodes and resume
   - JDBC RunStore / ArtifactStore for durable long-running tasks

6. Multi-Provider Schema Adapters
   - OpenAI
   - Azure OpenAI
   - DeepSeek
   - Tongyi Qwen
   - Doubao
   - Ollama

7. Existing-System Adaptation Skill
   - Scans Spring services
   - Recommends tool candidates
   - Generates `*AiTools` facades
   - Generates tests and README patches
   - Generates MCP provisioning plans

## Suggested Headlines

- Add AI Tool Governance to Enterprise Java Systems
- Not Just Tool Calling: Business Risk and Trust Boundaries for LLMs
- Spring AI Tool Adapter: Make Existing Spring Systems Agent-Ready
- From Tool Calling to Auditable, Approvable, Recoverable AI Execution

## 3-Minute Demo Script

1. Open `/chat`
   - Show conversation, task visualization, governance panel, and debug mode.

2. Run a low-risk tool
   - Show execution, audit record, and context snapshot.

3. Trigger a high-risk tool
   - Select `mock_create_order`
   - Click Run Tool
   - Show `PENDING_APPROVAL`
   - Approve and show resumed execution
   - Trigger again and reject to show the tool does not run

4. Open Tool Schema
   - Show provider-specific schema output for the same tool across OpenAI, DeepSeek, Tongyi, Ollama, and others.

5. Open Agent tab
   - Edit YAML Flow
   - Preview Phase / Step visualization
   - Start and stop at a Human node
   - Resume the run
   - Recover the run from RunStore

6. Open Audit tab
   - Filter by toolName, status, and tenantId
   - Inspect context before/after

## Screenshot Set

- `spring-ai-approval-demo-zh.png`: approval loop
- `agentweaver-demo-running.png`: agent running
- `agentweaver-demo-waiting.png`: human node waiting
- `agentweaver-demo-completed.png`: completed run
- `agentweaver-demo-audit.png`: audit review
- `agentweaver-demo-zh-completed.png`: Chinese UI

## Launch Post

I open-sourced Spring AI Tool Adapter, an enterprise-grade Java framework for governed AI tool calling.

The hard part is not exposing Java methods to an LLM. The hard part is answering enterprise questions:

- Who is allowed to call this tool?
- Does a high-risk action require human approval?
- How are inputs and return values masked?
- Can LLM retries accidentally create duplicate orders?
- How is user choice preserved across turns?
- Can we audit and replay what happened?
- Can long-running Agent tasks recover after restart?

The project includes:

- Spring Boot Starter
- `@AiTool` plus governance annotations
- Spring AI `ToolCallbackProvider` integration
- OpenAI / DeepSeek / Tongyi / Doubao / Ollama schema adapters
- Audit query UI
- Human approval loop
- YAML/JSON Agent Flow
- JDBC RunStore / ArtifactStore
- Codex Skill for adapting existing Spring systems

Core idea:

We do not just expose methods to LLMs. We annotate business intent, risk, and trust boundaries.

