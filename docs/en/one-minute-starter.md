# One-Minute Starter Quickstart

This guide shows the smallest Spring Boot app that uses `spring-ai-tool-adapter-starter`.

## 1. Add The Dependency

```xml
<dependency>
    <groupId>com.c8software.spring.ai</groupId>
    <artifactId>spring-ai-tool-adapter-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

If you want to expose a small HTTP catalog like the sample, also add:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## 2. Add One Tool

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

## 3. Start And Inspect

Run the included minimal example:

```bash
mvn -pl examples/starter-minimal spring-boot:run
```

Open:

- `http://localhost:8081/tools`
- `http://localhost:8081/tools/openai-schema`

The important point: the application only adds the starter and an annotated Spring Bean. The starter auto-registers the scanner, registry, schema conversion, executor, audit logger, permission checker, masking, context, and approval SPI defaults.

## Sample Project

See `examples/starter-minimal` for a complete copyable project.
