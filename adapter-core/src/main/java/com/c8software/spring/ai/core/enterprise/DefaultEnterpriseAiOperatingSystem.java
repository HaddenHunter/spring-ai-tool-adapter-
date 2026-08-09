package com.c8software.spring.ai.core.enterprise;

import java.util.LinkedHashMap;
import java.util.Map;

/** Default enterprise AI OS facade backed by marketplace and tenant SPIs. */
public class DefaultEnterpriseAiOperatingSystem implements EnterpriseAiOperatingSystem {
    private final TenantRegistry tenantRegistry;
    private final PromptMarketplace promptMarketplace;
    private final ToolMarketplace toolMarketplace;
    private final LearningFeedbackStore feedbackStore;

    public DefaultEnterpriseAiOperatingSystem(TenantRegistry tenantRegistry, PromptMarketplace promptMarketplace,
                                              ToolMarketplace toolMarketplace, LearningFeedbackStore feedbackStore) {
        this.tenantRegistry = tenantRegistry;
        this.promptMarketplace = promptMarketplace;
        this.toolMarketplace = toolMarketplace;
        this.feedbackStore = feedbackStore;
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("versionLine", "v3.x");
        result.put("status", "BASELINE");
        result.put("tenantCount", tenantRegistry.list().size());
        result.put("promptCount", promptMarketplace.list().size());
        result.put("toolCount", toolMarketplace.list().size());
        result.put("feedbackCount", feedbackStore.list(null, 1000).size());
        result.put("deploymentMode", "private-multi-tenant-ready");
        return result;
    }
}
