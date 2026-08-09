# Spring AI Tool Adapter 宣发包

## 一句话定位

Spring AI Tool Adapter 是面向企业 Java 系统的 AI Tool Calling 治理框架，把现有 Spring Bean 能力安全、可审计、可审批、可恢复地暴露给 LLM 和 Agent。

## 核心主张

我们不只是把方法暴露给 LLM，而是用注解声明业务意图、风险与信任边界。

## 目标用户

- 已经有 Spring Boot / Spring MVC 业务系统，希望快速接入 AI 工具调用的团队
- 需要权限、审计、审批、脱敏、幂等、多租户能力的企业应用团队
- 正在评估 Spring AI、LangChain4j、MCP、Agent 工作流的 Java 团队
- 想把已有系统自动改造成 AI-ready 系统的研发团队

## 主要卖点

1. 企业级 Tool Governance
   - `@AiTool` 暴露业务能力
   - `@AiToolRiskLevel` 声明风险
   - `@AiToolRequiresPermission` 声明权限
   - `@AiToolSensitive` 做敏感数据边界
   - `@AiToolIdempotent` 防止重复执行
   - 高风险工具进入人工审批闭环

2. 可审计，不是普通日志
   - traceId、tenantId、userId、toolName、cost、status
   - input/output hash
   - context before/after snapshot
   - 审计 UI 支持筛选和快照对比

3. Session + Context
   - 上下文是结构化业务状态，不是聊天记录拼接
   - 用户确认过的选择会进入 Context Fact
   - 支持多轮任务、回放和恢复

4. Spring AI 原生集成
   - 对接 Spring AI `ToolCallbackProvider`
   - 保留本框架的权限、审计、脱敏、审批、超时隔离
   - Demo 提供 ChatClient 风格审批闭环

5. Java 原生 Agent Harness
   - YAML/JSON Flow Spec
   - Phase / Step / Artifact / Checkpoint
   - Human node 和 Resume
   - JDBC RunStore / ArtifactStore，证明不是内存玩具

6. 多模型 Schema 适配
   - OpenAI
   - Azure OpenAI
   - DeepSeek
   - 通义千问
   - 豆包
   - Ollama

7. 现有系统自动适配 Skill
   - 扫描 Spring Service
   - 推荐可暴露方法
   - 生成 `*AiTools` facade
   - 生成测试和 README patch
   - 生成 MCP provisioning plan

## 推荐标题

- 给企业 Java 系统加一层 AI Tool Governance
- 不是把方法暴露给 LLM，而是声明业务风险与信任边界
- Spring AI Tool Adapter：让已有 Spring 系统安全进入 Agent 时代
- 从 Tool Calling 到可审批、可审计、可恢复的企业 AI 执行层

## 3 分钟 Demo 脚本

1. 打开 `/chat`
   - 展示左侧对话、中央任务流、右侧治理面板、底部 Debug。

2. 点击普通工具
   - 展示工具执行、审计记录、上下文快照。

3. 触发高风险工具
   - 选择 `mock_create_order`
   - 点击 Run Tool
   - 展示 `PENDING_APPROVAL`
   - 点击 Approve，展示 resume 后执行成功
   - 再触发一次并 Reject，展示拒绝后不执行

4. 打开 Tool Schema
   - 展示同一个工具在 OpenAI、DeepSeek、通义、Ollama 等 provider 下的 schema 输出。

5. 打开 Agent tab
   - 编辑 YAML Flow
   - Preview 生成 Phase / Step
   - Start 后卡在 Human node
   - Resume 后继续
   - Recover 展示从 RunStore 恢复

6. 打开 Audit tab
   - 按 toolName / status / tenantId 筛选
   - 展示 context before/after

## 建议截图

- `spring-ai-approval-demo-zh.png`: 审批闭环
- `agentweaver-demo-running.png`: Agent 执行中
- `agentweaver-demo-waiting.png`: Human node 等待
- `agentweaver-demo-completed.png`: Agent 完成
- `agentweaver-demo-audit.png`: 审计审核
- `agentweaver-demo-zh-completed.png`: 中文版本

## 发布帖

我开源了一个面向企业 Java 系统的 Spring AI Tool Adapter。

它解决的不是“怎么把 Java 方法暴露给 LLM”，而是更企业化的问题：

- 谁能调用这个工具？
- 高风险操作是否需要人工审批？
- 参数和返回值如何脱敏？
- LLM 重试会不会重复创建订单？
- 多轮对话里的用户选择如何持久化？
- 出问题后能不能审计和回放？
- Agent 长任务能不能恢复？

项目已经包含：

- Spring Boot Starter
- `@AiTool` 和治理注解
- Spring AI `ToolCallbackProvider` 集成
- OpenAI / DeepSeek / 通义 / 豆包 / Ollama schema 适配
- 审计查询 UI
- 人工审批闭环
- YAML/JSON Agent Flow
- JDBC RunStore / ArtifactStore
- 现有系统自动适配 Codex Skill

一句话：

我们不只是把方法暴露给 LLM，而是用注解声明业务意图、风险与信任边界。

## FAQ

### 和 Spring AI 是什么关系？

Spring AI 提供模型调用和 Tool Calling 抽象，本项目补企业治理层：注册、schema、权限、审批、脱敏、审计、上下文、回放、Agent 长任务。

### 和 LangChain4j 有什么差异？

本项目更聚焦 Spring 企业系统落地，把工具视为业务能力，而不是普通函数调用。治理、审计、审批、多租户、可恢复任务是核心设计。

### 是否会直接安装 MCP？

不会。默认只生成 MCP provisioning plan，真正启用外部能力需要审批和企业侧实现。

### 是否支持私有部署？

支持。项目是 Java/Spring 体系，核心能力可在私有网络内运行。

