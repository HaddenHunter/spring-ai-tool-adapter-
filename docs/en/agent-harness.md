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

## Minimal Usage

```java
AgentFlowDefinition flow = new AgentFlowDefinition("refund-flow", "Refund Flow", Arrays.asList(
        new AgentPhase("collect", "Collect", Arrays.asList(
                new AgentStep("query-order", "Query Order", AgentStepType.TOOL,
                        "query_order", "{\"orderId\":1001}", 0),
                new AgentStep("review", "Review", AgentStepType.REVIEW,
                        null, "{}", 1)
        ))
));

AgentRunState state = agentHarness.start(flow, executionContext);
```

## Boundary

- Harness owns flow execution, checkpoints, artifacts, review, and repair loops.
- Tool Adapter owns tool governance.
- Spring AI owns model calls, Advisors, RAG, and Memory.
- AgentWeaver can become an external executor later, not a hard core dependency.

## Next Steps

- YAML / JSON Flow Spec parsing.
- ArtifactStore persistence SPI.
- JDBC RunStore.
- Demo UI for Phase, Step, Artifact, and Checkpoint.
- External AgentWeaver CLI executor.
