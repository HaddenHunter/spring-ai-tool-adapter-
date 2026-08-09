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

## Demo UI

`adapter-demo` 已接入 Agent Harness，可以在底部调试区的 `Agent` 标签页查看和操作一次声明式 flow 运行。

- `GET /api/agent/sample-flow`：返回 demo YAML Flow Spec。
- `POST /api/agent/start`：根据 YAML / JSON Flow Spec 创建 run 并执行到完成或人工等待节点。
- `GET /api/agent/runs/{runId}`：读取 run state、checkpoint 和 artifact。
- `POST /api/agent/runs/{runId}/resume`：从当前 checkpoint 继续执行，demo 中会把当前人工等待步骤标记为已完成后恢复。

UI 会展示：

- Flow：phase 和 step 结构。
- Phase / Step：当前执行位置。
- Artifact：工具步骤产物。
- Checkpoint：每步执行后的断点。
- Resume：人工节点或中断后的恢复按钮。

宣发截图：

![AgentWeaver compatible demo](../agentweaver-demo-promo.png)

状态截图：

| 执行中 | 等待人工 / 可恢复 |
| --- | --- |
| ![Agent running](../agentweaver-demo-running.png) | ![Agent waiting](../agentweaver-demo-waiting.png) |

| 已完成 | 审计审核 |
| --- | --- |
| ![Agent completed](../agentweaver-demo-completed.png) | ![Agent audit review](../agentweaver-demo-audit.png) |

中文界面截图：

![Agent Chinese completed](../agentweaver-demo-zh-completed.png)

截图入口：

- `/chat?promo=agentweaver&state=running`
- `/chat?promo=agentweaver&state=waiting`
- `/chat?promo=agentweaver&state=completed`
- `/chat?promo=agentweaver&state=audit`
- `/chat?lang=zh&promo=agentweaver&state=completed`

## 下一步

- 外部 AgentWeaver CLI executor。
