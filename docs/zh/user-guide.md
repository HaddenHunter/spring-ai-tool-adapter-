# Spring AI Tool Adapter 中文使用手册

## 1. 项目定位

Spring AI Tool Adapter 是一个面向企业级 AI Tool Calling 的 Java / Spring Boot 适配框架。它不是简单地把 Java 方法暴露给大模型，而是把业务能力、权限、风险、审计、脱敏、上下文和 MCP 扩展边界一起声明出来。

核心理念：

- Tool 是业务能力，不是普通函数。
- 每一次 Tool 调用都必须可观测、可审计。
- 高风险动作必须支持 Human-in-the-loop。
- 多轮对话的上下文是结构化业务状态，不是聊天记录拼接。
- 框架只提供契约、SPI 和默认实现，企业业务逻辑由使用方实现。

## 2. 模块结构

```text
spring-ai-tool-adapter
├── adapter-core   核心框架模块
├── adapter-demo   可运行 Demo 与 Chat UI
├── skills         给 Codex 使用的自动接入 Skill
├── docs           中英文使用手册与功能说明
└── *.yaml / *.md  架构、边界、路线图与 phase prompt
```

`adapter-core` 提供：

- 注解与治理元数据
- Tool 注册与发现
- Schema 转换
- 执行引擎
- 权限、脱敏、审计 SPI
- Session + Context
- 任务编排基础模型
- Semantic MCP provisioning SPI

`adapter-demo` 提供：

- Mock 工具
- Chat 页面
- 工具列表接口
- Schema / Prompt / Audit 调试接口
- MCP 语义规划示例接口

## 3. 环境要求

- JDK 8 或 JDK 17
- Maven 3.8+
- Spring Boot 2.x / 3.x 项目均可参考接入

本仓库当前使用 Maven 多模块结构。

## 4. 快速启动

在项目根目录执行：

```bash
mvn test
```

启动 Demo：

```bash
mvn -pl adapter-demo spring-boot:run
```

打开：

```text
http://localhost:8080/chat
```

常用 Demo 接口：

```text
GET  /api/tools
GET  /api/debug/schema
GET  /api/debug/prompt
GET  /api/governance
GET  /api/audit/logs
POST /api/chat
POST /api/mcp/semantic-plan
```

## 5. 在已有 Spring 系统中接入

### 5.1 添加依赖

如果你把本项目发布到内部 Maven 仓库，可在业务系统中添加 starter：

```xml
<dependency>
    <groupId>com.c8software.spring.ai</groupId>
    <artifactId>spring-ai-tool-adapter-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

starter 会自动引入 `adapter-core` 并注册自动配置。若是源码多模块开发，也可以直接依赖 `adapter-core`。

### 5.2 启用自动配置

starter 会自动加载默认自动配置类：

```java
com.c8software.spring.ai.core.config.AiToolAutoConfiguration
```

如果业务系统没有使用 starter，或自动配置没有生效，可以显式导入：

```java
@Import(AiToolAutoConfiguration.class)
```

### 5.3 声明一个 AI Tool

推荐写一个薄 Facade，把已有 Service 包装成 AI 可调用业务能力：

```java
@Component
@ToolGroup("order")
public class OrderAiTools {

    private final OrderService orderService;

    public OrderAiTools(OrderService orderService) {
        this.orderService = orderService;
    }

    @AiTool(name = "refund_order", description = "Create a refund for an order")
    @AiToolRequiresPermission("order:refund")
    @AiToolRiskLevel(RiskLevel.HIGH)
    @AiToolAudit(AuditLevel.FULL)
    @AiToolIdempotent(key = "#orderId")
    @AiToolContextKey(store = "lastRefundOrderId")
    public RefundResult refundOrder(
            @AiToolParam(description = "order id") Long orderId,
            @AiToolParam(description = "refund amount") BigDecimal amount,
            @AiToolSensitive(type = SensitiveType.OPERATOR_ID) Long operatorId
    ) {
        return orderService.refund(orderId, amount, operatorId);
    }
}
```

注意：

- 不要把业务逻辑写进注解处理器。
- 不要把 Repository 的原始 CRUD 方法直接暴露给 LLM。
- 高风险操作必须声明风险等级、权限、审计和幂等策略。

## 6. 治理注解说明

| 注解 | 用途 |
| --- | --- |
| `@AiTool` | 声明方法是 AI 可调用工具 |
| `@AiToolRequiresPermission` | 声明调用权限 |
| `@AiToolRiskLevel` | 声明风险等级 |
| `@AiToolAudit` | 声明审计级别 |
| `@AiToolParam` | 声明参数语义 |
| `@AiToolSensitive` | 声明敏感参数 |
| `@AiToolIdempotent` | 声明幂等 key |
| `@AiToolRollback` | 声明回滚方法 |
| `@AiToolVisibility` | 声明工具可见性 |
| `@AiToolVersion` | 声明工具版本 |
| `@AiToolContextKey` | 将参数或结果绑定到会话上下文 |

风险等级：

```java
LOW       // 查询
MEDIUM    // 通知、轻量业务动作
HIGH      // 资金、订单、数据变更
CRITICAL  // 系统配置、权限、批量导出
```

审计等级：

```java
NONE
BASIC
FULL
```

可见性：

```java
PUBLIC
INTERNAL
DEPRECATED
```

## 7. 执行 Tool

核心接口：

```java
ToolResult execute(String toolName, String jsonArguments, ExecutionContext executionContext);
```

示例：

```java
ExecutionContext context = new ExecutionContext(
        "user-1",
        "tenant-1",
        "trace-1",
        Collections.singleton("order:refund"),
        Instant.now()
);

ToolResult result = toolExecutor.execute(
        "refund_order",
        "{\"orderId\":1001,\"amount\":20.5,\"operatorId\":9}",
        context
);
```

执行链路：

```text
JSON 参数
 -> 参数绑定
 -> 权限校验
 -> 高风险审批
 -> 幂等命中检查
 -> 参数脱敏
 -> MethodHandle 调用业务方法
 -> 幂等结果存储
 -> 审计记录
 -> 返回 ToolResult
```

### 7.1 Human-in-the-loop 审批

`HIGH` 和 `CRITICAL` 风险工具会通过 `ToolApprovalManager` 触发审批。

默认自动配置使用安全默认值：如果没有提供真实 `HumanInTheLoop` 实现，高风险工具会返回审批未通过，不会自动执行。

业务系统可以提供自己的 Bean：

```java
@Bean
public HumanInTheLoop humanInTheLoop() {
    return request -> approvalService.requestApproval(request);
}
```

### 7.2 幂等保护

带 `@AiToolIdempotent` 的工具会使用 `IdempotencyStore` 缓存成功结果。重复调用命中同一个 key 时，框架直接返回历史结果，避免重复退款、重复发券或重复创建订单。

默认实现是内存存储，生产环境建议替换为 Redis 或数据库实现。

## 8. 生成 Tool Schema

内置 Schema Converter：

- OpenAI
- Azure OpenAI
- DeepSeek
- 通义千问
- 豆包
- Ollama

示例：

```java
ToolSchemaConverter converter = new OpenAIFunctionSchemaConverter();
Map<String, Object> schema = converter.convert(toolDefinition);
```

导出能力：

- Java 基础类型到 JSON Schema
- 枚举值
- required 参数
- `@Min` / `@Max`
- 参数描述

## 9. Session + Context 使用

上下文不是聊天历史，而是结构化业务状态。

核心对象：

- `ConversationSession`
- `TaskContext`
- `ContextFact`
- `UserChoiceTracker`
- `ContextCompressor`
- `ContextSnapshot`

用户确认过的选择必须写入 Context Fact：

```java
userChoiceTracker.confirmChoice(
        taskContext,
        "selectedCustomerId",
        "1001",
        "user-confirmed"
);
```

绑定当前线程上下文：

```java
ConversationContextHolder.bind(session, taskContext);
try {
    toolExecutor.execute(toolName, arguments, executionContext);
} finally {
    ConversationContextHolder.clear();
}
```

审计记录会包含执行前后的上下文快照 hash，便于事后回放。

## 10. Semantic MCP Provisioning

当用户用自然语言说“帮我接入客户 CRM”“接入财务发票系统”“接入短信通知系统”时，框架可以生成一个 MCP 接入计划。

核心接口：

- `McpCapabilityCatalog`
- `McpSemanticMatcher`
- `McpProvisioningPlanner`

示例：

```java
McpProvisionPlan plan = planner.plan(new McpSemanticRequest(
        "session-1",
        "tenant-1",
        "user-1",
        "帮我接入客户 CRM 和工单系统",
        Collections.singletonList("mcp:provision:crm")
));
```

重要边界：

- 只生成 provisioning plan。
- 不自动安装外部 MCP。
- 不自动启用外部系统连接。
- 高风险 MCP 能力必须要求审批。
- 缺少权限时必须返回 `PERMISSION_REQUIRED`。

## 11. 使用 Codex Skill 自动接入已有系统

本仓库提供：

```text
skills/spring-ai-adapt-existing-system
```

这个 Skill 给 Codex 使用，用于扫描已有 Spring 项目并自动生成：

- Tool Facade
- 治理注解
- 上下文绑定
- MCP provisioning catalog
- 单元测试

典型提示词：

```text
Use $spring-ai-adapt-existing-system to analyze this Spring project and generate governed Spring AI Tool Adapter integration code.
```

## 12. 扩展点

| SPI | 用途 |
| --- | --- |
| `PermissionChecker` | 接入企业权限系统 |
| `SensitiveMasker` | 自定义脱敏规则 |
| `AuditLogger` | 接入 MySQL / PostgreSQL / ES / ClickHouse |
| `ToolExecutor` | 替换执行逻辑 |
| `ToolRegistry` | 自定义工具来源 |
| `ToolSchemaConverter` | 支持新模型 Schema |
| `ContextCompressor` | 自定义上下文压缩策略 |
| `UserChoiceTracker` | 自定义用户选择持久化 |
| `McpCapabilityCatalog` | 自定义 MCP 能力目录 |
| `McpSemanticMatcher` | 自定义语义匹配器 |
| `ToolApprovalManager` | 自定义高风险工具审批策略 |
| `HumanInTheLoop` | 接入企业审批系统 |
| `IdempotencyStore` | 接入 Redis / DB 幂等存储 |
| `ToolVisibilityFilter` | 控制工具是否对 LLM 可见 |

## 13. 持久化审计日志

默认没有数据库时使用 `AsyncAuditLogger`。当 Spring 容器存在 `DataSource` 时，自动配置会启用 `JdbcAuditLogger` 并创建 `ai_tool_audit_log` 表。

查询接口：

```text
GET /api/audit/logs?traceId=...&toolName=...&callerUser=...&tenantId=...&status=...&limit=100
```

## 14. 生产落地建议

- 审计日志不要只放内存，生产环境应落库。
- 权限校验应接入 Spring Security、RBAC、租户和数据权限。
- 高风险 Tool 应接入审批流。
- 幂等 key 应落到 Redis / DB 等可共享存储。
- MCP provisioning 只做计划，实际安装和授权必须走管理员审批。
- 对外开放前应进行敏感字段和越权访问测试。
