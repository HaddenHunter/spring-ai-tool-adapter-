package com.c8software.spring.ai.demo.tool;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolAudit;
import com.c8software.spring.ai.core.annotation.AiToolContextKey;
import com.c8software.spring.ai.core.annotation.AiToolIdempotent;
import com.c8software.spring.ai.core.annotation.AiToolParam;
import com.c8software.spring.ai.core.annotation.AiToolRequiresPermission;
import com.c8software.spring.ai.core.annotation.AiToolRiskLevel;
import com.c8software.spring.ai.core.annotation.AiToolSensitive;
import com.c8software.spring.ai.core.annotation.AiToolVersion;
import com.c8software.spring.ai.core.annotation.AiToolVisibility;
import com.c8software.spring.ai.core.annotation.AuditLevel;
import com.c8software.spring.ai.core.annotation.RiskLevel;
import com.c8software.spring.ai.core.annotation.SensitiveType;
import com.c8software.spring.ai.core.annotation.ToolGroup;
import com.c8software.spring.ai.core.annotation.ToolVisibility;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ToolGroup("demo")
public class DemoTools {

    @AiTool(
            name = "mock_query_user_balance",
            description = "Query mock user balance",
            paramDescriptions = "user id"
    )
    @AiToolRequiresPermission("finance:read")
    @AiToolRiskLevel(RiskLevel.LOW)
    @AiToolAudit(AuditLevel.FULL)
    @AiToolVersion("1.0.0")
    public Map<String, Object> queryUserBalance(@AiToolParam(description = "user id") String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("balance", 1024.50);
        return result;
    }

    @AiTool(
            name = "mock_send_sms",
            description = "Send mock SMS",
            paramDescriptions = {"mobile", "content"}
    )
    @AiToolRiskLevel(RiskLevel.MEDIUM)
    @AiToolAudit(AuditLevel.FULL)
    @AiToolIdempotent(key = "#mobile + ':' + #content")
    public String sendSms(@AiToolSensitive(type = SensitiveType.MOBILE) String mobile,
                          @AiToolParam(description = "message content") String content) {
        return "SMS accepted for " + mobile + ": " + content;
    }

    @AiTool(
            name = "mock_create_order",
            description = "Create a mock order",
            paramDescriptions = {"user id", "amount"}
    )
    @AiToolRiskLevel(RiskLevel.HIGH)
    @AiToolAudit(AuditLevel.FULL)
    @AiToolIdempotent(key = "#userId + ':' + #amount")
    @AiToolContextKey(store = "lastOrderId")
    public Map<String, Object> createOrder(@AiToolContextKey(store = "selectedCustomerId", confirmed = true) String userId,
                                           @AiToolParam(description = "order amount") Double amount) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", "MOCK-ORDER-1");
        result.put("userId", userId);
        result.put("amount", amount);
        return result;
    }

    @AiTool(
            name = "mock_query_weather",
            description = "Query mock weather",
            paramDescriptions = "city"
    )
    @AiToolRiskLevel(RiskLevel.LOW)
    @AiToolVisibility(ToolVisibility.PUBLIC)
    public String queryWeather(String city) {
        return city + " sunny 26C";
    }

    @AiTool(
            name = "mock_query_complaint_customer",
            description = "Query mock complaint customer profile",
            paramDescriptions = "mobile"
    )
    @AiToolRiskLevel(RiskLevel.LOW)
    @AiToolVisibility(ToolVisibility.INTERNAL)
    public Map<String, Object> queryComplaintCustomer(@AiToolSensitive(type = SensitiveType.MOBILE) String mobile) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mobile", mobile);
        result.put("level", "VIP");
        result.put("complaints", 2);
        return result;
    }
}
