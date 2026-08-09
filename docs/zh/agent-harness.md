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
- `AgentFlowSpecParser`：把 JSON / YAML 声明式 Flow Spec 解析成 `AgentFlowDefinition`。
- `ArtifactStore`：持久化步骤产物。
- `JdbcAgentRunStore`：用 JDBC 保存 run state 和 checkpoint。
- `JdbcArtifactStore`：用 JDBC 保存 artifact。

## 最小使用方式

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

`arguments` 可以写成对象，解析器会转成工具执行需要的 JSON 字符串；也可以直接使用 `argumentsJson`。

## 设计边界

- Harness 负责流程、断点、产物、审查、修复循环。
- Tool Adapter 负责工具治理。
- Spring AI 负责模型、Advisor、RAG、Memory。
- AgentWeaver 可作为未来的外部 executor，不进入 core 强依赖。

## 持久化

默认情况下使用内存实现：

- `InMemoryAgentRunStore`
- `InMemoryArtifactStore`

当 Spring 容器中存在 `DataSource` 时，自动配置会优先注册：

- `JdbcAgentRunStore`
- `JdbcArtifactStore`

JDBC RunStore 保存：

- run id
- flow id
- status
- current phase / step
- completed step ids
- attributes
- checkpoints

ArtifactStore 保存：

- artifact id
- run id
- step id
- artifact type
- artifact content JSON
- created time

## 下一步

- Demo UI 展示 Phase、Step、Artifact、Checkpoint。
- 外部 AgentWeaver CLI executor。
