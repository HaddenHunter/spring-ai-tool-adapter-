# v1.x Baseline

v1.x is the Business AI Hub baseline. Its purpose is to turn individual tools into session-aware, governed business capabilities that can participate in multi-turn business processes.

## Completion Status

Status: baseline implemented.

## Completed Capabilities

| Area | Status | Notes |
| --- | --- | --- |
| Business AI hub | Baseline | `BusinessAiHub` and `DefaultBusinessAiHub` bind user input, session, task context, tool execution, and replay. |
| Multi-turn session binding | Baseline | Uses `ConversationSessionStore` and transport-neutral `ConversationSession`. |
| Structured task context | Baseline | Each hub request creates a `TaskContext` with task id, type, status, current step, and user utterance. |
| Tool grouping | Baseline | Existing `ToolGroup` and metadata are used for grouping and tool list views. |
| Permission-aware execution | Baseline | Reuses `PermissionChecker` and `ExecutionContext.permissions`. |
| Human approval boundary | Baseline | Reuses `ToolApprovalManager` and `HumanInTheLoop` for `HIGH` and `CRITICAL` tools. |
| Replay | Baseline | `ConversationReplayStore` records replayable `ConversationTurn` entries with before/after context snapshots. |
| Demo APIs | Baseline | `/api/v1/status`, `/api/replay/{sessionId}`, and `/api/chat` use the v1 hub. |
| Demo UI | Baseline | Debug panel includes replay and governance panel shows v1 status. |

## v1 Demo Endpoints

```text
GET  /api/v1/status
GET  /api/replay/{sessionId}
POST /api/chat
```

## Core Classes

```text
BusinessAiHub
DefaultBusinessAiHub
BusinessAiHubRequest
BusinessAiHubResponse
ConversationTurn
ConversationReplayStore
InMemoryConversationReplayStore
```

## v2.x Boundary

The following work belongs to v2.x:

- Durable workflow runtime.
- Multi-agent collaboration.
- Long-running task queue.
- Human node callbacks and resumed execution.
- DAG rollback execution.
- Persistent task runtime tables.

## Production Extension Points

- Replace `ConversationReplayStore` with database or search storage.
- Replace `ConversationSessionStore` with Redis or database.
- Replace `PermissionChecker` with Spring Security, RBAC, tenant scopes, or data scopes.
- Replace `HumanInTheLoop` with enterprise approval workflow.
- Add UI for replay comparison and approval operations.
