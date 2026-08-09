# Agent Harness 说明

`adapter-agent-harness` 是 v2.x 的 Java 原生 Agent 工作流骨架。它吸收 AgentWeaver 的 Flow / Artifact / Checkpoint / Review / Repair 思路，但不把 Node.js 运行时放进 Java core。

## 当前能力

- `AgentFlowDefinition`：定义一个业务 Agent 流程。
- `AgentPhase`：流程阶段。
- `AgentStep`：阶段内步骤。
- `AgentRunState`：运行状态。
- `AgentCheckpoint`：每步执行后的断点。
- `AgentArtifact`：每步产物。
- `AgentStepExecutor`：步骤执行器 SPI。
- `ToolAgentStepExecutor`：调用本项目治理后的 `ToolExecutor`。
- `ReviewGate`：审查门。
- `AgentRepairLoop`：修复循环扩展点。
- `HumanAgentStepExecutor`：人工节点，进入 `WAITING`。

## 最小使用方式

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

## 设计边界

- Harness 负责流程、断点、产物、审查、修复循环。
- Tool Adapter 负责工具治理。
- Spring AI 负责模型、Advisor、RAG、Memory。
- AgentWeaver 可作为未来的外部 executor，不进入 core 强依赖。

## 下一步

- YAML / JSON Flow Spec 解析。
- ArtifactStore 持久化 SPI。
- RunStore JDBC 实现。
- Demo UI 展示 Phase、Step、Artifact、Checkpoint。
- 外部 AgentWeaver CLI executor。
