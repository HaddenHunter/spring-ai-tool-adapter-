# Social Posts

## Short Chinese Post

开源了一个 Spring AI Tool Adapter。

它不是简单把 Java 方法暴露给 LLM，而是给企业系统补上 Tool Governance：

- 权限
- 风险等级
- 人工审批
- 参数和返回值脱敏
- 幂等
- 审计回放
- Session + Context
- YAML/JSON Agent Flow
- JDBC RunStore 恢复
- Spring AI ToolCallbackProvider 集成

核心一句话：

我们不只是把方法暴露给 LLM，而是用注解声明业务意图、风险与信任边界。

## Long Chinese Post

最近做了一个面向企业 Java 系统的开源项目：Spring AI Tool Adapter。

很多 Tool Calling demo 只解决“LLM 能不能调用 Java 方法”。但企业真正会问的是：

- 谁能调用？
- 高风险操作要不要审批？
- 审计里能不能看到当时的上下文？
- 手机号、身份证、银行卡会不会进日志？
- LLM retry 会不会重复下单？
- 多轮对话里用户已经确认的选择会不会丢？
- Agent 长任务能不能恢复？

这个项目把这些问题放进了 Spring 体系：

- `@AiTool` 暴露业务能力
- 治理注解声明权限、风险、审计、脱敏、幂等、回滚、可见性、版本、上下文绑定
- Spring Boot Starter 一分钟接入
- Spring AI ToolCallbackProvider 桥接
- OpenAI、DeepSeek、通义、豆包、Ollama schema 对比
- 人工审批闭环：待审批 -> 批准/拒绝 -> resume
- 审计 UI：按 traceId、toolName、status、tenantId 筛选，并查看 context before/after
- Java 原生 Agent Harness：Flow / Phase / Step / Artifact / Checkpoint / Resume / Recover
- Codex Skill：扫描已有 Spring Service，自动推荐和生成 AI Tool facade

我更想强调的是世界观：

Tool 不是函数，而是业务能力。
Context 不是聊天记录，而是结构化业务状态。
Audit 不是日志，而是可回放的证据链。

## English Post

I open-sourced Spring AI Tool Adapter.

It is an enterprise-grade Java framework for governed AI tool calling in existing Spring systems.

The hard part is not exposing a method to an LLM. The hard part is making that method safe for enterprise execution:

- permissions
- risk levels
- human approvals
- sensitive data masking
- idempotency
- audit replay
- session and structured context
- YAML/JSON Agent Flow
- JDBC-backed run recovery
- Spring AI ToolCallbackProvider integration

Core idea:

We do not just expose methods to LLMs. We annotate business intent, risk, and trust boundaries.

## Taglines

- Enterprise AI Tool Governance for Spring.
- Make existing Spring systems Agent-ready.
- Tool Calling with approvals, audit, masking, and recovery.
- From Java methods to governed business capabilities.

