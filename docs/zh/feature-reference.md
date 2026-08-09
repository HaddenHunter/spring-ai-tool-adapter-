# Spring AI Tool Adapter 功能说明

## 1. 总览

本项目提供企业级 AI Tool Adapter，用于把 Spring Bean 中的业务能力安全、可治理、可审计地暴露给 LLM / Agent。

```text
LLM / Agent
  -> Tool Schema
  -> Tool Adapter
  -> 权限 / 脱敏 / 审计 / 上下文
  -> Spring Bean 业务系统
```

核心能力：

1. 注解与治理元数据
2. 工具注册与发现
3. Schema 生成
4. 工具执行引擎
5. 权限控制
6. 参数和返回值脱敏
7. 审计日志
8. Session + Context
9. 任务编排
10. 多模型适配
11. Semantic MCP provisioning
12. Codex Skill 自动接入已有系统
13. P0 企业治理：持久审计、审批、幂等、可见性过滤、超时隔离、返回值脱敏
14. Spring Boot Starter 自动装配
15. Maven 发布配置

## 2. Spring Boot Starter

`spring-ai-tool-adapter-starter` 用于让业务系统只添加一个依赖即可启用框架能力。

starter 内容：

- 依赖 `adapter-core`
- 提供 Spring Boot 2 `META-INF/spring.factories`
- 提供 Spring Boot 3 `AutoConfiguration.imports`
- 自动加载 `AiToolAutoConfiguration`

## 3. 注解体系

`@AiTool` 是入口注解，用于声明 Java 方法是 AI 可调用工具。

治理注解包括：

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

`ToolMetadata` 是不可变对象，包含分组、权限、审计级别、风险等级、可见性、版本、幂等配置、回滚配置、上下文绑定、返回值敏感类型、超时配置和扩展属性。

## 4. 工具注册与发现

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

## 5. Schema 生成

`ToolSchemaConverter` 负责把 `ToolDefinition` 转换成模型可消费的 Tool Schema。

内置实现：

- `OpenAIFunctionSchemaConverter`
- `AzureOpenAISchemaConverter`
- `DeepSeekSchemaConverter`
- `TongyiQwenSchemaConverter`
- `DoubaoSchemaConverter`
- `OllamaSchemaConverter`

支持基础类型、枚举、简单 `List<String>`，并导出 `@NotNull`、`@Min`、`@Max` 等校验信息。

## 6. 执行引擎

核心接口：

```java
public interface ToolExecutor {
    ToolResult execute(String toolName, String jsonArguments, ExecutionContext executionContext);
}
```

默认实现 `DefaultToolExecutor` 的步骤：

1. 从 `ToolRegistry` 查询工具。
2. 反序列化 JSON 参数。
3. 转换 Java 参数类型。
4. 执行权限校验。
5. 生成参数脱敏摘要。
6. 触发高风险审批。
7. 检查幂等命中。
8. 通过 `ToolInvocationExecutor` 隔离调用业务方法。
9. 通过 `ResultMasker` 脱敏返回值。
10. 存储幂等结果。
11. 生成 `ToolResult`。
12. 写入审计记录。
13. 包装异常或走 fallback。

## 7. 超时执行隔离

`ToolInvocationExecutor` 是真实业务方法调用的隔离 SPI。

默认实现：

```java
TimeoutToolInvocationExecutor
```

行为：

- 使用独立工作线程执行业务方法。
- 使用 `ToolMetadata.timeoutMillis` 控制等待时间。
- 超时后取消 Future 并抛出 `AiToolExecutionException`。
- 错误码为 `AIT_EXEC_TIMEOUT`。
- 调用方线程不会被慢工具长期拖住。

配置：

```yaml
ai:
  tool:
    default-timeout-millis: 10000
```

企业可替换该 SPI，实现线程池隔离、远程沙箱、队列执行、熔断、租户级资源限制等策略。

## 8. 参数和返回值脱敏

SPI：

```java
SensitiveMasker
ResultMasker
```

参数级脱敏：

```java
public UserInfo getUser(@AiToolSensitive(type = SensitiveType.ID_CARD) String idCard) {
    ...
}
```

返回值脱敏：

```java
@AiTool(name = "query_mobile", description = "Query mobile")
@AiToolSensitive(type = SensitiveType.MOBILE)
public String queryMobile(Long userId) {
    return "13812345678";
}
```

方法级 `@AiToolSensitive` 会进入 `ToolMetadata.resultSensitiveType`，默认 `DefaultResultMasker` 会在返回 `ToolResult` 和写审计摘要前脱敏整个返回值。

内置敏感类型：

- `MOBILE`
- `ID_CARD`
- `BANK_CARD`
- `NAME`
- `PASSWORD`
- `OPERATOR_ID`
- `CUSTOM`

## 9. 审计日志

SPI：

```java
AuditLogger
```

默认实现：

- `AsyncAuditLogger`
- `JdbcAuditLogger`

当 Spring 容器中存在 `DataSource` 时，自动配置会优先创建 `JdbcAuditLogger`，并初始化 `ai_tool_audit_log` 表。

审计字段：

- traceId
- toolName
- callerUser
- tenantId
- inputHash
- outputHash
- costMs
- status
- errorMessage
- eventType
- contextBeforeHash
- contextAfterHash
- timestamp

## 10. 审批、幂等与可见性

审批：

- `ToolApprovalManager` 判断高风险工具是否可以执行。
- `HumanInTheLoop` 是企业审批系统接入点。
- starter 默认采用安全拒绝实现，未接入审批时不执行高风险工具。

幂等：

- `@AiToolIdempotent` 声明幂等 key。
- `IdempotencyKeyResolver` 解析 key。
- `IdempotencyStore` 存储成功结果。
- 默认内存存储，生产建议替换为 Redis 或数据库。

可见性：

- `PUBLIC` 可见。
- `INTERNAL` 需要 `tool:internal` 权限。
- `DEPRECATED` 不可见。

## 11. Session + Context

上下文模块记录：

- 当前会话是谁
- 当前任务是什么
- 流程走到哪一步
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

## 12. Maven 发布

根 POM 已配置：

- 项目 URL、SCM、license、developer 元数据
- GitHub Packages `distributionManagement`
- `maven-source-plugin`
- `maven-javadoc-plugin`
- `maven-deploy-plugin`

发布前在 `~/.m2/settings.xml` 配置 `github` server，然后执行：

```bash
mvn -DskipTests deploy
```

目标仓库：

```text
https://maven.pkg.github.com/HaddenHunter/spring-ai-tool-adapter-
```

## 13. 当前实现程度

已实现：

- Core annotations、治理注解、注册、Schema、执行、审计、Context、MCP planning、starter、Demo。
- P0 企业能力：持久审计、人工审批接入点、幂等保护、可见性过滤、超时隔离、返回值脱敏。
- Maven 发布配置。

仍建议增强：

- 发布到 Maven Central 的签名和 staging 流程。
- Redis / DB 级幂等存储。
- 对象字段级返回值脱敏。
- 更完整的审批工作流和回滚执行器。
