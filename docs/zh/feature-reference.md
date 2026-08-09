# Spring AI Tool Adapter 功能说明

## 1. 总览

本项目提供一个企业级 AI Tool Adapter，用于把 Spring Bean 中的业务能力安全、可治理、可审计地暴露给 LLM / Agent。

总体链路：

```text
LLM / Agent
  -> Tool Schema
  -> Tool Adapter
  -> 权限 / 脱敏 / 审计 / 上下文
  -> Spring Bean 业务系统
```

核心模块：

1. 注解与治理元数据
2. 工具注册与发现
3. Schema 生成
4. 工具执行引擎
5. 权限控制
6. 参数脱敏
7. 审计日志
8. Session + Context
9. 任务编排
10. 多模型适配
11. Semantic MCP provisioning
12. Codex Skill 自动接入旧系统
13. P0 企业治理增强：持久化审计、审批、幂等、可见性过滤

## 2. 注解体系

### 2.1 能力注解

`@AiTool` 是入口注解，用于声明某个 Java 方法是 AI 可调用工具。

字段：

- `name`：全局唯一工具名
- `description`：业务语义描述
- `paramDescriptions`：参数描述兼容字段
- `enabled`：是否启用
- `requiresPermission`：兼容旧权限字段
- `auditLevel`：兼容旧审计字段

### 2.2 治理注解

治理注解是企业落地的核心：

- `@AiToolRequiresPermission`
- `@AiToolRiskLevel`
- `@AiToolAudit`
- `@AiToolParam`
- `@AiToolSensitive`
- `@AiToolIdempotent`
- `@AiToolRollback`
- `@AiToolVisibility`
- `@AiToolVersion`
- `@AiToolContextKey`

这些注解只表达声明式元数据，不承载业务逻辑。

### 2.3 元数据沉淀

注解会被 `AiToolRegistrar` 和 `ToolGovernanceAnnotationProcessor` 解析成：

- `ToolDefinition`
- `ToolMetadata`
- `ToolParameter`

`ToolMetadata` 是不可变对象，包含：

- 分组
- 权限
- 审计级别
- 风险等级
- 可见性
- 版本
- 幂等配置
- 回滚配置
- 上下文绑定
- 超时配置
- 扩展属性

## 3. 工具注册与发现

`AiToolRegistrar` 实现 Spring `BeanPostProcessor`，在 Bean 初始化后扫描带 `@AiTool` 的方法。

注册过程：

```text
扫描 Bean
 -> 找到 @AiTool 方法
 -> 解析治理注解
 -> 构建 ToolParameter
 -> 构建 ToolMetadata
 -> 构建 ReflectionToolDefinition
 -> 注册到 ToolRegistry
```

关键能力：

- 工具名全局唯一校验
- 按名称查询工具
- 按分组查询工具
- 线程安全注册表
- MethodHandle 缓存
- Spring AOP / CGLIB 目标类扫描

## 4. Schema 生成

`ToolSchemaConverter` 负责把 `ToolDefinition` 转换成模型可消费的 Tool Schema。

内置实现：

- `OpenAIFunctionSchemaConverter`
- `AzureOpenAISchemaConverter`
- `DeepSeekSchemaConverter`
- `TongyiQwenSchemaConverter`
- `DoubaoSchemaConverter`
- `OllamaSchemaConverter`

支持类型：

- `String`
- `Integer` / `Long`
- `BigDecimal` / `Double` / `Float`
- `Boolean`
- `Enum`
- 简单 `List<String>`

支持校验导出：

- `@NotNull`
- `@Min`
- `@Max`

## 5. 执行引擎

核心接口：

```java
public interface ToolExecutor {
    ToolResult execute(String toolName, String jsonArguments, ExecutionContext executionContext);
}
```

默认实现：

```java
DefaultToolExecutor
```

执行步骤：

1. 从 `ToolRegistry` 查询工具
2. JSON 参数反序列化
3. 参数类型转换
4. 权限校验
5. 参数脱敏
6. 高风险审批
7. 幂等命中检查
8. MethodHandle 调用业务方法
9. 幂等结果存储
10. 生成 `ToolResult`
11. 写入审计记录
12. 捕获并包装异常

## 6. 权限控制

SPI：

```java
PermissionChecker
```

默认实现：

```java
DefaultPermissionChecker
```

默认策略：

- 从 `ToolMetadata.requiresPermission` 读取权限 key
- 从 `ExecutionContext.permissions` 判断是否拥有权限
- 不通过时抛出 `AiToolSecurityException`

生产建议：

- 接入 Spring Security
- 接入 RBAC
- 接入租户和数据权限
- 支持 SpEL 或企业内部权限表达式

## 7. 参数脱敏

SPI：

```java
SensitiveMasker
```

默认实现：

```java
DefaultSensitiveMasker
```

敏感类型：

- `MOBILE`
- `ID_CARD`
- `BANK_CARD`
- `NAME`
- `PASSWORD`
- `OPERATOR_ID`
- `CUSTOM`

脱敏作用范围：

- 审计输入摘要
- 后续可扩展到返回值脱敏

## 8. 审计日志

SPI：

```java
AuditLogger
```

默认实现：

```java
AsyncAuditLogger
```

数据库实现：

```java
JdbcAuditLogger
```

当 Spring 容器中存在 `DataSource` 时，自动配置会优先创建 `JdbcAuditLogger`，并初始化 `ai_tool_audit_log` 表。

审计字段：

- `traceId`
- `toolName`
- `callerUser`
- `tenantId`
- `inputHash`
- `outputHash`
- `costMs`
- `status`
- `errorMessage`
- `eventType`
- `contextBeforeHash`
- `contextAfterHash`
- `timestamp`

意义：

- 可以追踪谁在什么时候调用了什么工具
- 可以对比执行前后的上下文状态
- 可以支持事后回放和合规检查

查询能力：

- `traceId`
- `toolName`
- `callerUser`
- `tenantId`
- `status`
- `limit`

## 8.1 审批与幂等

审批：

- `ToolApprovalManager` 负责判断高风险工具是否能执行。
- `HumanInTheLoop` 是企业审批系统接入点。
- 自动配置默认使用安全拒绝实现，未接入审批时不执行高风险工具。

幂等：

- `@AiToolIdempotent` 声明幂等 key。
- `IdempotencyKeyResolver` 解析 key。
- `IdempotencyStore` 存储成功结果。
- 默认使用内存存储，生产建议替换为 Redis / DB。

## 8.2 Tool 可见性过滤

`ToolVisibilityFilter` 控制工具是否进入工具列表和 Schema。

默认策略：

- `PUBLIC` 可见
- `INTERNAL` 需要 `tool:internal` 权限
- `DEPRECATED` 不可见

## 9. Session + Context

上下文模块解决：

- 当前会话是谁
- 当前任务是什么
- 当前流程走到哪一步
- 用户刚才确认了什么选择
- 工具失败后状态是否仍然可恢复

核心类型：

- `ConversationSession`
- `ConversationSessionStore`
- `TaskContext`
- `TaskStatus`
- `ContextFact`
- `UserChoiceTracker`
- `ContextCompressor`
- `ContextSnapshot`
- `ConversationContextHolder`

状态模型：

```text
Session: sessionId / tenantId / userId / role / model
Task: taskId / taskType / taskStatus / currentStep / pendingApproval
Choice: selectedCustomerId / selectedTemplateId / selectedAmount / userOverrides
```

约束：

- `adapter-core` 不依赖 HTTP Session
- 确认过的用户选择不可被静默覆盖
- 压缩上下文不能丢失 confirmed facts
- 工具失败不能清空上下文

## 10. 任务编排

任务编排模块提供轻量 DAG 模型：

- `TaskDefinition`
- `TaskNode`
- `TaskEdge`
- `TaskNodeType`
- `TaskExecutor`
- `HumanInTheLoop`
- `ApprovalRequest`
- `ApprovalResponse`

节点类型：

- `TOOL`
- `CONDITION`
- `HUMAN_APPROVAL`
- `SUB_TASK`

当前实现重点是基础模型和 Tool 节点执行，后续可扩展条件分支、审批等待、回滚和长任务。

## 11. Semantic MCP Provisioning

该模块用于把自然语言集成需求转换成 MCP 接入计划。

核心类型：

- `McpCapabilityCatalog`
- `McpCapabilityDescriptor`
- `McpSemanticMatcher`
- `McpProvisioningPlanner`
- `McpProvisionPlan`

默认目录包含：

- CRM Customer MCP
- Finance Readonly MCP
- Messaging MCP

返回状态：

- `NO_MATCH`
- `PERMISSION_REQUIRED`
- `PENDING_APPROVAL`
- `READY_TO_PROVISION`

边界：

- 不自动安装 MCP
- 不自动授权外部系统
- 只生成计划
- 企业可替换 catalog 和 matcher

## 12. Demo 功能

Demo 工具：

- `mock_query_user_balance`
- `mock_send_sms`
- `mock_create_order`
- `mock_query_weather`
- `mock_query_complaint_customer`

Demo 页面包含：

- 左侧对话区
- 工具调用卡片
- 当前任务可视化
- 治理面板
- 调试面板
- Prompt / Schema / Audit 查看

## 13. Codex Skill

本项目内置：

```text
skills/spring-ai-adapt-existing-system
```

它用于帮助别人把已有 Spring 系统自动改造成可使用本框架的 AI Tool 系统。

生成内容：

- Tool facade
- 治理注解
- Context key
- MCP catalog
- 测试

## 14. 当前边界

当前版本聚焦 v0.x 工具适配器能力：

- 已提供核心注解、注册、Schema、执行、审计、上下文、MCP 计划和 Demo。
- 尚未提供生产级持久化审计存储。
- 尚未提供真实审批流实现。
- 尚未提供真实 MCP 安装器。
- 尚未提供完整长任务运行时。

这些能力通过 SPI 留给企业实现。
