# Spring AI Tool Adapter 中文使用手册

## 1. 项目定位

Spring AI Tool Adapter 是面向企业 AI Tool Calling 的 Java / Spring Boot 框架。它不是简单把 Java 方法暴露给大模型，而是把业务意图、权限、风险、审计、脱敏、上下文和 MCP 扩展边界声明为可治理契约。

核心原则：

- Tool 是业务能力，不是普通函数。
- 每一次 Tool 调用都必须可观测、可审计。
- 高风险操作必须支持 Human-in-the-loop。
- 多轮上下文是结构化业务状态，不是聊天记录拼接。
- 框架提供契约、SPI 和默认实现，业务逻辑仍留在使用方系统中。

## 2. 模块结构

```text
spring-ai-tool-adapter
|-- adapter-core                         核心框架模块
|-- spring-ai-tool-adapter-starter       Spring Boot starter
|-- adapter-demo                         可运行 Demo 与 Chat UI
|-- skills                               给 Codex 使用的自动接入 Skill
|-- docs                                 中文和英文手册
`-- *.yaml / *.md                        架构、边界、路线图和 phase prompt
```

`adapter-core` 提供注解、注册、Schema、执行、权限、脱敏、审计、Session + Context、任务编排模型和 Semantic MCP SPI。

## 3. 快速启动

环境要求：

- JDK 8 或 JDK 17
- Maven 3.8+
- Spring Boot 2.x / 3.x 项目均可接入

运行测试：

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

## 4. 在已有 Spring 系统中接入

发布到 Maven 仓库后，业务系统只需要添加 starter：

```xml
<dependency>
    <groupId>com.c8software.spring.ai</groupId>
    <artifactId>spring-ai-tool-adapter-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

starter 会自动加载：

```java
com.c8software.spring.ai.core.config.AiToolAutoConfiguration
```

如果没有使用 starter，也可以显式导入：

```java
@Import(AiToolAutoConfiguration.class)
```

## 5. 声明一个 AI Tool

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

## 6. 超时执行隔离

默认执行器通过 `ToolInvocationExecutor` 调用业务方法。内置实现 `TimeoutToolInvocationExecutor` 会把实际业务调用放入独立工作线程，并使用 `ToolMetadata.timeoutMillis` 限制等待时间。

默认超时时间：

```yaml
ai:
  tool:
    default-timeout-millis: 10000
```

超时后调用方会收到 `AiToolExecutionException`，错误码为 `AIT_EXEC_TIMEOUT`。框架会尝试中断工作线程，避免 LLM 请求线程被慢工具长期占用。生产环境可以替换 `ToolInvocationExecutor`，接入线程池隔离、任务队列、熔断或远程执行沙箱。

## 7. 参数与返回值脱敏

参数脱敏：

```java
public UserInfo getUser(@AiToolSensitive(type = SensitiveType.ID_CARD) String idCard) {
    ...
}
```

返回值脱敏：

```java
@AiTool(name = "query_user_mobile", description = "Query user mobile")
@AiToolSensitive(type = SensitiveType.MOBILE)
public String queryUserMobile(Long userId) {
    return "13812345678";
}
```

方法级 `@AiToolSensitive` 表示整个返回值在进入 `ToolResult` 和审计摘要前都会被脱敏。默认实现由 `ResultMasker` 和 `SensitiveMasker` 组合完成；企业系统可以替换 SPI，实现对象字段级脱敏或更严格的数据分类策略。

## 8. 审计与上下文

默认无数据库时使用 `AsyncAuditLogger`。当 Spring 容器存在 `DataSource` 时，自动配置会启用 `JdbcAuditLogger` 并创建 `ai_tool_audit_log` 表。

审计记录包括：

- traceId、toolName、callerUser、tenantId
- inputHash、outputHash
- costMs、status、errorMessage
- contextBeforeHash、contextAfterHash

Session + Context 记录结构化业务状态，包括会话、任务状态和用户已确认选择。上下文快照会参与审计，支持事后回放。

## 9. 发布到 Maven 仓库

根 POM 已配置 GitHub Packages 发布信息、SCM、license、source jar 和 javadoc jar。

在 `~/.m2/settings.xml` 配置 server：

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

发布：

```bash
mvn -DskipTests deploy
```

目标仓库：

```text
https://maven.pkg.github.com/HaddenHunter/spring-ai-tool-adapter-
```

## 10. 扩展点

- `ToolRegistry`：自定义工具来源。
- `ToolExecutor`：替换整体执行流程。
- `ToolInvocationExecutor`：替换超时、隔离和真实调用策略。
- `ResultMasker`：替换返回值脱敏策略。
- `SensitiveMasker`：替换具体掩码规则。
- `AuditLogger`：替换审计存储。
- `PermissionChecker`：接入企业权限系统。
- `ToolApprovalManager` / `HumanInTheLoop`：接入审批系统。
- `IdempotencyStore`：接入 Redis 或数据库。
- `ContextCompressor`：接入上下文压缩策略。
