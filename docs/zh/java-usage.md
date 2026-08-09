# Java 使用说明

这份文档面向把 Spring AI Tool Adapter 接入到现有 Java / Spring Boot 系统的开发者，重点说明依赖、注解、执行、Schema、治理 SPI 和生产配置。

## 1. 添加 Maven 依赖

推荐业务系统直接使用 starter：

```xml
<dependency>
    <groupId>com.c8software.spring.ai</groupId>
    <artifactId>spring-ai-tool-adapter-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

如果只想引用核心能力，也可以直接依赖 core：

```xml
<dependency>
    <groupId>com.c8software.spring.ai</groupId>
    <artifactId>adapter-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

GitHub Packages 仓库地址：

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/HaddenHunter/spring-ai-tool-adapter-</url>
    </repository>
</repositories>
```

## 2. Spring Boot 自动装配

starter 会自动加载：

```java
com.c8software.spring.ai.core.config.AiToolAutoConfiguration
```

如果没有使用 starter，可以显式导入：

```java
@Configuration
@Import(AiToolAutoConfiguration.class)
public class AiToolConfig {
}
```

## 3. 编写工具 Facade

不要直接把 Repository CRUD 暴露给 LLM。推荐新增一个薄 Facade，把已有 Service 包装成可治理的业务能力。

```java
package com.company.order.ai;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolAudit;
import com.c8software.spring.ai.core.annotation.AiToolContextKey;
import com.c8software.spring.ai.core.annotation.AiToolIdempotent;
import com.c8software.spring.ai.core.annotation.AiToolParam;
import com.c8software.spring.ai.core.annotation.AiToolRequiresPermission;
import com.c8software.spring.ai.core.annotation.AiToolRiskLevel;
import com.c8software.spring.ai.core.annotation.AiToolSensitive;
import com.c8software.spring.ai.core.annotation.AuditLevel;
import com.c8software.spring.ai.core.annotation.RiskLevel;
import com.c8software.spring.ai.core.annotation.SensitiveType;
import com.c8software.spring.ai.core.annotation.ToolGroup;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
    @AiToolContextKey(store = "lastRefundOrderId", confirmed = true)
    public RefundResult refundOrder(
            @AiToolParam(description = "order id") Long orderId,
            @AiToolParam(description = "refund amount") BigDecimal amount,
            @AiToolSensitive(type = SensitiveType.OPERATOR_ID) Long operatorId
    ) {
        return orderService.refund(orderId, amount, operatorId);
    }
}
```

## 4. 执行工具

在业务系统中注入 `ToolExecutor`：

```java
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.execution.ToolResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;

@RestController
public class AiToolController {
    private final ToolExecutor toolExecutor;

    public AiToolController(ToolExecutor toolExecutor) {
        this.toolExecutor = toolExecutor;
    }

    @PostMapping("/internal/ai/tool/refund")
    public ToolResult refund(@RequestBody String jsonArguments) {
        ExecutionContext context = new ExecutionContext(
                "user-1",
                "tenant-1",
                "trace-1",
                Collections.singleton("order:refund"),
                Instant.now()
        );
        return toolExecutor.execute("refund_order", jsonArguments, context);
    }
}
```

调用参数是 JSON 字符串：

```json
{
  "orderId": 1001,
  "amount": 20.50,
  "operatorId": 9
}
```

## 5. 生成 Tool Schema

```java
ToolDefinition definition = toolRegistry.get("refund_order");
ToolSchemaConverter converter = new OpenAIFunctionSchemaConverter();
Map<String, Object> schema = converter.convert(definition);
```

内置 converter：

- `OpenAIFunctionSchemaConverter`
- `AzureOpenAISchemaConverter`
- `DeepSeekSchemaConverter`
- `TongyiQwenSchemaConverter`
- `DoubaoSchemaConverter`
- `OllamaSchemaConverter`

## 6. 权限、审批和幂等

默认 `DefaultPermissionChecker` 会读取 `ExecutionContext.permissions`，判断是否包含工具声明的权限。

生产系统可替换自己的权限实现：

```java
@Bean
public PermissionChecker permissionChecker(SecurityService securityService) {
    return (definition, context) -> {
        String permission = definition.getMetadata().getRequiresPermission();
        if (permission != null && !permission.isEmpty()) {
            securityService.check(context.getCurrentUser(), context.getTenantId(), permission);
        }
    };
}
```

高风险工具接入审批：

```java
@Bean
public HumanInTheLoop humanInTheLoop(ApprovalService approvalService) {
    return request -> approvalService.requestApproval(request);
}
```

幂等声明：

```java
@AiToolIdempotent(key = "#orderId")
```

## 7. 超时隔离

默认使用 `TimeoutToolInvocationExecutor`，业务方法在工作线程中执行。

```yaml
ai:
  tool:
    default-timeout-millis: 10000
```

超时错误码：

```text
AIT_EXEC_TIMEOUT
```

## 8. 参数和返回值脱敏

参数脱敏：

```java
public UserInfo getUser(@AiToolSensitive(type = SensitiveType.ID_CARD) String idCard) {
    return userService.findByIdCard(idCard);
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

默认返回：

```text
138****5678
```

需要对象字段级脱敏时，替换 `ResultMasker`。

## 9. 审计日志

没有 `DataSource` 时使用 `AsyncAuditLogger`。存在 `DataSource` 时自动启用 `JdbcAuditLogger`，并创建 `ai_tool_audit_log` 表。

审计会记录：

- traceId
- toolName
- callerUser
- tenantId
- inputHash
- outputHash
- costMs
- status
- contextBeforeHash
- contextAfterHash

## 10. Session + Context

上下文不是聊天记录，而是结构化业务状态。

```java
ConversationContextHolder.bind(session, taskContext);
try {
    toolExecutor.execute(toolName, jsonArguments, executionContext);
} finally {
    ConversationContextHolder.clear();
}
```

记录用户确认选择：

```java
userChoiceTracker.confirmChoice(
        taskContext,
        "selectedCustomerId",
        "1001",
        "user-confirmed"
);
```

## 11. 最小生产接入清单

1. 引入 `spring-ai-tool-adapter-starter`。
2. 为业务 Service 编写 AI Facade。
3. 给每个工具补齐权限、风险、审计、参数说明。
4. 高风险工具补齐审批和幂等。
5. 接入企业 `PermissionChecker`。
6. 接入 `HumanInTheLoop`。
7. 使用数据库审计或自定义 `AuditLogger`。
8. 为慢工具配置合理 timeout。
9. 对参数和返回值做脱敏测试。
10. 只把 `PUBLIC` 工具暴露给 LLM。
