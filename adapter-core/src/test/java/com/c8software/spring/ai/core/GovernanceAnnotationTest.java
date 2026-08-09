package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.annotation.AiToolAudit;
import com.c8software.spring.ai.core.annotation.AiToolContextKey;
import com.c8software.spring.ai.core.annotation.AiToolIdempotent;
import com.c8software.spring.ai.core.annotation.AiToolParam;
import com.c8software.spring.ai.core.annotation.AiToolRequiresPermission;
import com.c8software.spring.ai.core.annotation.AiToolRiskLevel;
import com.c8software.spring.ai.core.annotation.AiToolRollback;
import com.c8software.spring.ai.core.annotation.AiToolSensitive;
import com.c8software.spring.ai.core.annotation.AiToolVersion;
import com.c8software.spring.ai.core.annotation.AiToolVisibility;
import com.c8software.spring.ai.core.annotation.AuditLevel;
import com.c8software.spring.ai.core.annotation.RiskLevel;
import com.c8software.spring.ai.core.annotation.SensitiveType;
import com.c8software.spring.ai.core.annotation.ToolVisibility;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GovernanceAnnotationTest {

    @Test
    void enrichesToolMetadataFromGovernanceAnnotations() {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new RefundTools(), "refundTools");

        ToolDefinition definition = registry.get("refund_order");

        assertThat(definition.getMetadata().getRequiresPermission()).isEqualTo("order:refund");
        assertThat(definition.getMetadata().getRiskLevel()).isEqualTo("HIGH");
        assertThat(definition.getMetadata().getAuditLevel()).isEqualTo("FULL");
        assertThat(definition.getMetadata().isIdempotent()).isTrue();
        assertThat(definition.getMetadata().getIdempotentKey()).isEqualTo("#orderId");
        assertThat(definition.getMetadata().isRollbackable()).isTrue();
        assertThat(definition.getMetadata().getRollbackMethod()).isEqualTo("cancelRefund");
        assertThat(definition.getMetadata().getVisibility()).isEqualTo("INTERNAL");
        assertThat(definition.getMetadata().getVersion()).isEqualTo("2.0.0");
        assertThat(definition.getMetadata().getContextKey()).isEqualTo("lastRefundOrderId");
        assertThat(definition.getParameters().get(0).getDescription()).isEqualTo("order id");
        assertThat(definition.getParameters().get(2).getSensitiveType()).isEqualTo(SensitiveType.OPERATOR_ID);
    }

    static class RefundTools {

        @AiTool(name = "refund_order", description = "Create refund")
        @AiToolRequiresPermission("order:refund")
        @AiToolRiskLevel(RiskLevel.HIGH)
        @AiToolAudit(AuditLevel.FULL)
        @AiToolIdempotent(key = "#orderId")
        @AiToolRollback(rollbackMethod = "cancelRefund")
        @AiToolVisibility(ToolVisibility.INTERNAL)
        @AiToolVersion("2.0.0")
        @AiToolContextKey(store = "lastRefundOrderId")
        public String refundOrder(@AiToolParam(description = "order id") Long orderId,
                                  @AiToolParam(description = "amount") BigDecimal amount,
                                  @AiToolSensitive(type = SensitiveType.OPERATOR_ID) Long operatorId) {
            return "ok";
        }
    }
}
