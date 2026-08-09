package com.c8software.spring.ai.quickstart;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolAudit;
import com.c8software.spring.ai.core.annotation.AiToolParam;
import com.c8software.spring.ai.core.annotation.AiToolRequiresPermission;
import com.c8software.spring.ai.core.annotation.AiToolRiskLevel;
import com.c8software.spring.ai.core.annotation.AuditLevel;
import com.c8software.spring.ai.core.annotation.RiskLevel;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public Map<String, Object> queryBalance(@AiToolParam(description = "Account id") String accountId,
                                            @AiToolParam(description = "Currency code") String currency) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("accountId", accountId);
        result.put("currency", currency == null || currency.trim().isEmpty() ? "CNY" : currency);
        result.put("balance", new BigDecimal("1024.50"));
        return result;
    }
}
