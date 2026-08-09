# Agent Harness

`adapter-agent-harness` is the Java-native v2.x Agent workflow baseline. It adopts the Flow / Artifact / Checkpoint / Review / Repair ideas from AgentWeaver without embedding a Node.js runtime into Java core.

## Current Capabilities

- `AgentFlowDefinition`: business agent flow definition.
- `AgentPhase`: flow phase.
- `AgentStep`: phase step.
- `AgentRunState`: run state.
- `AgentCheckpoint`: resumable checkpoint after each step.
- `AgentArtifact`: step output artifact.
- `AgentStepExecutor`: step executor SPI.
- `ToolAgentStepExecutor`: invokes governed adapter `ToolExecutor`.
- `ReviewGate`: review boundary.
- `AgentRepairLoop`: repair extension point.
- `HumanAgentStepExecutor`: human node that enters `WAITING`.
- `AgentFlowSpecParser`: parses JSON / YAML declarative Flow Specs into `AgentFlowDefinition`.
- `ArtifactStore`: persists step artifacts.
- `JdbcAgentRunStore`: persists run state and checkpoints through JDBC.
- `JdbcArtifactStore`: persists artifacts through JDBC.

## Minimal Usage

```java
AgentFlowDefinition flow = agentFlowSpecParser.parseYaml(yaml);

AgentRunState state = agentHarness.start(flow, executionContext);
```

## YAML Flow Spec

```yaml
id: refund-flow
name: Refund Flow
phases:
  - id: collect
    name: Collect
    steps:
      - id: query-order
        name: Query Order
        type: tool
        toolName: query_order
        arguments:
          orderId: 1001
      - id: review
        name: Review
        type: review
        maxRepairAttempts: 1
      - id: approval
        name: Approval
        type: human
```

## JSON Flow Spec

```json
{
  "id": "refund-flow",
  "name": "Refund Flow",
  "phases": [
    {
      "id": "collect",
      "name": "Collect",
      "steps": [
        {
          "id": "query-order",
          "name": "Query Order",
          "type": "tool",
          "toolName": "query_order",
          "arguments": {
            "orderId": 1001
          }
        }
      ]
    }
  ]
}
```

`arguments` can be an object; the parser converts it to the JSON string required by tool execution. Use `argumentsJson` when the value is already serialized.

## Boundary

- Harness owns flow execution, checkpoints, artifacts, review, and repair loops.
- Tool Adapter owns tool governance.
- Spring AI owns model calls, Advisors, RAG, and Memory.
- AgentWeaver can become an external executor later, not a hard core dependency.

## Persistence

By default, the harness uses in-memory implementations:

- `InMemoryAgentRunStore`
- `InMemoryArtifactStore`

When a `DataSource` exists in the Spring context, auto-configuration prefers:

- `JdbcAgentRunStore`
- `JdbcArtifactStore`

JDBC RunStore persists:

- run id
- flow id
- status
- current phase / step
- completed step ids
- attributes
- checkpoints

ArtifactStore persists:

- artifact id
- run id
- step id
- artifact type
- artifact content JSON
- created time

## Demo UI

`adapter-demo` now connects to Agent Harness. Open the bottom debug area and choose the `Agent` tab to inspect and operate a declarative flow run.

- `GET /api/agent/sample-flow`: returns the demo YAML Flow Spec.
- `POST /api/agent/start`: creates a run from a YAML / JSON Flow Spec and executes until completion or a human waiting node.
- `GET /api/agent/runs/{runId}`: reads run state, checkpoints, and artifacts.
- `POST /api/agent/runs/{runId}/resume`: resumes from the current checkpoint. In the demo, the current human waiting step is marked completed before resuming.

The UI shows:

- Flow: phase and step structure.
- Phase / Step: current execution position.
- Artifact: tool step outputs.
- Checkpoint: resumable execution points after steps.
- Resume: button for human nodes or interrupted runs.

Promotion screenshot:

![AgentWeaver compatible demo](../agentweaver-demo-promo.png)

State screenshots:

| Running | Waiting / Resume |
| --- | --- |
| ![Agent running](../agentweaver-demo-running.png) | ![Agent waiting](../agentweaver-demo-waiting.png) |

| Completed | Audit Review |
| --- | --- |
| ![Agent completed](../agentweaver-demo-completed.png) | ![Agent audit review](../agentweaver-demo-audit.png) |

Screenshot routes:

- `/chat?promo=agentweaver&state=running`
- `/chat?promo=agentweaver&state=waiting`
- `/chat?promo=agentweaver&state=completed`
- `/chat?promo=agentweaver&state=audit`

## Next Steps

- External AgentWeaver CLI executor.
