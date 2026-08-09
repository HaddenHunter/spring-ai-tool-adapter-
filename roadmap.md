# Roadmap

This project evolves from a tool-calling adapter into an enterprise AI operating system.

## v0.x - Tool Adapter

Current focus: make Spring Bean methods reliably callable by LLMs.

- Tool registration: annotation scanning, CGLIB-aware method discovery, thread-safe registry.
- Schema: OpenAI-compatible schema plus DeepSeek, Tongyi Qwen, Doubao, and Ollama adapters.
- Execution: JSON argument binding, permission checks, sensitive-data masking, timeout boundaries.
- Audit: asynchronous audit records with trace, cost, status, and hashed input/output.
- Chat UI: local demo page, mock tools, audit query endpoints, and Prometheus metrics.

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
