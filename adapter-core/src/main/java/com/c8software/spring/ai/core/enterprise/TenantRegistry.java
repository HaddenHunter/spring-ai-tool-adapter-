package com.c8software.spring.ai.core.enterprise;

import java.util.List;

/** Registry for tenant isolation profiles. */
public interface TenantRegistry {
    void save(TenantProfile profile);

    TenantProfile get(String tenantId);

    List<TenantProfile> list();
}
