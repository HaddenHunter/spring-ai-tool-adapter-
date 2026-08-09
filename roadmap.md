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

Goal: turn tools into governed business capabilities.

- Multi-turn conversation context with replayable history.
- Tool grouping by domain, role, and tenant.
- Permission policies for business roles and data scopes.
- Human approval gates for sensitive operations.
- Conversation and tool-call replay for support, audit, and debugging.

## v2.x - Tasks and Agents

Goal: coordinate longer-running work beyond one chat turn.

- Workflow orchestration with DAG tasks and rollback hooks.
- Multi-agent collaboration for planning, execution, review, and escalation.
- Long-task lifecycle: queued, running, waiting, resumed, completed, failed.
- Human nodes for approvals, clarification, exception handling, and handoff.

## v3.x - Enterprise AI Operating System

Goal: make AI capability reusable, governable, and deployable across the organization.

- Self-learning feedback loops for prompts, tool quality, and task outcomes.
- Prompt marketplace with versioning, ownership, approval, and rollback.
- Tool marketplace with schema validation, permissions, examples, and usage analytics.
- Private deployment and multi-tenant isolation for enterprise environments.
