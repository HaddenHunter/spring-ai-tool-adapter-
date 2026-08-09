# Spring AI Tool Adapter

Enterprise-oriented Java adapter that exposes annotated Spring Bean methods as AI-callable tools.

We don't just expose methods to LLMs. We annotate business intent, risk, and trust boundaries.

## Quick Start

1. Build: `mvn test`
2. Run: `mvn -pl adapter-demo spring-boot:run`
3. Open: `http://localhost:8080/chat`

## Example Tools

- `mock_query_user_balance`
- `mock_send_sms`
- `mock_create_order`
- `mock_query_weather`
- `mock_query_complaint_customer`

## Governance Contract

Tool annotations describe what can be called. Governance annotations describe who may call it, how risky it is, how it is audited, which parameters are sensitive, whether calls are idempotent, whether an action is reversible, and how results bind into conversation context.

## Enterprise P0 Governance

- Persistent audit logging through `JdbcAuditLogger` when a `DataSource` is available.
- Human-in-the-loop approval gate for `HIGH` and `CRITICAL` risk tools.
- Idempotency protection for tools annotated with `@AiToolIdempotent`.
- Visibility filtering so `INTERNAL` and `DEPRECATED` tools do not pollute LLM schemas.

## Semantic MCP Skills

The adapter can turn natural-language integration needs into an MCP provisioning plan. The default implementation matches semantic capability tags and returns risk, permission, and approval status. It does not directly install external MCP servers; enterprise implementations can replace the catalog and matcher through SPI.

## Codex Skill for Existing Systems

This repository includes `skills/spring-ai-adapt-existing-system`, a reusable Codex skill for teams adopting the framework in an existing Spring application. It guides Codex to scan current services, generate governed `@AiTool` facades, bind session context, add semantic MCP provisioning plans, and create tests.

## Product Direction

- v0.x: Tool Adapter covering tool registration, schema, execution, audit, and Chat UI.
- v1.x: Business AI hub covering multi-turn dialogue, tool groups, permissions, approval, and replay.
- v2.x: Tasks and agents covering workflow orchestration, multi-agent collaboration, long tasks, and human nodes.
- v3.x: Enterprise AI operating system covering self-learning, prompt marketplace, tool marketplace, private deployment, and multi-tenancy.

## Documents

- [中文使用手册](docs/zh/user-guide.md)
- [中文功能说明](docs/zh/feature-reference.md)
- [English User Guide](docs/en/user-guide.md)
- [English Feature Reference](docs/en/feature-reference.md)
- [Architecture](architecture.md)
- [Functional Plan](functional-plan.md)
- [Roadmap](roadmap.md)
- [World Model](world-model.yaml)
- [Capability Boundaries](capability-boundaries.yaml)
- [Phase Prompts](phase-prompts.yaml)
