# 一分钟 Starter 接入

这份文档展示最小 Spring Boot 应用如何接入 `spring-ai-tool-adapter-starter`。

## 1. 添加依赖

```xml
<dependency>
    <groupId>com.c8software.spring.ai</groupId>
    <artifactId>spring-ai-tool-adapter-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

如果你也想像示例一样通过 HTTP 查看工具列表，再添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 2. 写一个工具

```java
@Component
public class AccountTools {

    @AiTool(
            name = "query_account_balance",
            description = "Query an account balance",
            paramDescriptions = {"accountId=Account id", "currency=Currency code"}
    )
    @AiToolRequiresPermission("account:read")
    @AiToolRiskLevel(RiskLevel.LOW)
    @AiToolAudit(AuditLevel.FULL)
    public Map<String, Object> queryBalance(String accountId, String currency) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accountId", accountId);
        result.put("currency", currency);
        result.put("balance", new BigDecimal("1024.50"));
        return result;
    }
}
```

## 3. 启动并查看

运行仓库内置的最小示例：

```bash
mvn -pl examples/starter-minimal spring-boot:run
```

打开：

- `http://localhost:8081/tools`
- `http://localhost:8081/tools/openai-schema`

关键点：业务应用只需要添加 starter，再写一个带 `@AiTool` 的 Spring Bean。starter 会自动注册扫描器、工具注册表、Schema 转换、执行器、审计、权限检查、脱敏、上下文和审批 SPI 默认实现。

## 完整示例

完整可复制项目见 `examples/starter-minimal`。
