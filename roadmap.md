# Roadmap

This project evolves from a tool-calling adapter into an enterprise AI operating system.

## v0.x - Tool Adapter

Status: complete for the 0.x baseline. The focus is making Spring Bean methods reliably callable by LLMs with governance, audit, demo UI, starter packaging, and Maven publishing.

- Tool registration: annotation scanning, CGLIB-aware method discovery, thread-safe registry. Done.
- Schema: OpenAI-compatible schema plus DeepSeek, Tongyi Qwen, Doubao, and Ollama adapters. Done.
- Execution: JSON argument binding, permission checks, input and return-value masking, timeout isolation, idempotency, and approval boundary. Done.
- Audit: asynchronous and JDBC audit records with trace, cost, status, hashed input/output, and context hashes. Done.
- Chat UI: local demo page, mock tools, audit query endpoints, debug panels, governance panel, v0 status endpoint, and Prometheus metrics. Done.
- Packaging: Spring Boot starter and GitHub Packages Maven publishing. Done.

## v1.x - Business AI Hub

Status: baseline implemented. The goal is to turn tools into governed business capabilities with session-aware context, grouped visibility, approval boundaries, and replay.

- Multi-turn conversation context with replayable turns. Baseline done.
- Tool grouping by domain, role, and tenant. Baseline done through `ToolGroup`, metadata, and visibility filters.
- Permission policies for business roles and data scopes. Baseline done through `PermissionChecker`; enterprise policy engines remain extension points.
- Human approval gates for sensitive operations. Baseline done through `ToolApprovalManager` and `HumanInTheLoop`.
- Conversation and tool-call replay for support, audit, and debugging. Baseline done through `ConversationReplayStore` and context snapshots.

## v2.x - Tasks and Agents

Status: Java-native Agent Harness baseline implemented. The goal is to coordinate longer-running work beyond one chat turn.

- Workflow orchestration with phases and steps. Baseline done through `adapter-agent-harness`.
- Artifacts and checkpoints after each step. Baseline done through `AgentArtifact` and `AgentCheckpoint`.
- Long-task lifecycle: created, running, waiting, completed, failed. Baseline done through `AgentRunStatus`.
- Human nodes for approvals, clarification, exception handling, and handoff. Baseline done through `HumanAgentStepExecutor`.
- Review and repair loop boundary. Baseline done through `ReviewGate` and `AgentRepairLoop`.
- Declarative YAML/JSON Flow Spec parsing. Baseline done through `AgentFlowSpecParser`.
- Future: JDBC run store, artifact persistence, external AgentWeaver CLI executor, and UI run visualization.

## v3.x - Enterprise AI Operating System

Status: baseline implemented. The goal is to make AI capability reusable, governable, and deployable across the organization.

- Self-learning feedback loops for prompts, tool quality, and task outcomes. Baseline done through `LearningFeedbackStore`.
- Prompt marketplace with versioning, ownership, approval, and rollback. Baseline done through `PromptMarketplace`.
- Tool marketplace with schema validation, permissions, examples, and usage analytics. Baseline done through `ToolMarketplace`.
- Private deployment and multi-tenant isolation for enterprise environments. Baseline done through `TenantRegistry`.
