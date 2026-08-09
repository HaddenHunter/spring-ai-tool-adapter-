# v0.x Completion

v0.x is the Tool Adapter baseline. Its goal is to make Spring Bean business capabilities reliably callable by LLMs while keeping execution observable, governed, and easy to integrate into existing Spring Boot applications.

## Completion Status

Status: complete.

## Completed Capabilities

| Area | Status | Notes |
| --- | --- | --- |
| Annotation contract | Complete | `@AiTool` plus governance annotations for permission, risk, audit, sensitive data, idempotency, rollback, visibility, version, and context keys. |
| Tool registration | Complete | Spring `BeanPostProcessor`, CGLIB-aware scanning, immutable metadata, global name uniqueness, group lookup. |
| Schema conversion | Complete | OpenAI, Azure OpenAI, DeepSeek, Tongyi Qwen, Doubao, and Ollama converters. |
| Execution engine | Complete | JSON binding, permission checks, approval boundary, idempotency, timeout isolation, result wrapping, fallback. |
| Sensitive data | Complete | Parameter masking and method-level return-value masking. |
| Audit | Complete | Async audit, JDBC audit auto-configuration, hashed input/output, context snapshot hashes. |
| Context | Complete | Transport-neutral session, task context, immutable facts, user choice tracker, context compressor, audit snapshots. |
| Demo UI | Complete | Chat workbench, task visualization, governance panel, debug prompt/schema/audit views. |
| Demo APIs | Complete | Tools, schema, prompt, governance, audit logs, chat, stream, semantic MCP plan, v0 status. |
| Observability | Complete | Micrometer counters and Prometheus exposure. |
| Starter | Complete | Spring Boot 2 and Spring Boot 3 auto-configuration entry points. |
| Publishing | Complete | GitHub Packages distribution management and deployed `0.1.0` artifacts. |
| Documentation | Complete | README, English guide, Chinese guide, Java usage guide, feature reference, roadmap. |

## Demo Endpoints

```text
GET  /chat
GET  /api/tools
GET  /api/debug/schema
GET  /api/debug/prompt
GET  /api/governance
GET  /api/v0/status
GET  /api/audit/logs
GET  /api/chat/stream
POST /api/chat
POST /api/mcp/semantic-plan
GET  /actuator/prometheus
```

## Maven Artifacts

Published version:

```text
0.1.0
```

Artifacts:

```text
com.c8software.spring.ai:spring-ai-tool-adapter:0.1.0
com.c8software.spring.ai:adapter-core:0.1.0
com.c8software.spring.ai:spring-ai-tool-adapter-starter:0.1.0
com.c8software.spring.ai:adapter-demo:0.1.0
```

## v1.x Boundary

The following work belongs to v1.x and should not be mixed into the v0.x baseline:

- Persistent multi-turn conversation store beyond the in-memory default.
- Enterprise approval workflow UI and callback lifecycle.
- Full replay UI.
- Tenant-aware policy management.
- Durable idempotency store implementation.
- Advanced task orchestration runtime.
- MCP marketplace installation workflow.
