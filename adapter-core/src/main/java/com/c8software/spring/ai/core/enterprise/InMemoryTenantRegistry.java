package com.c8software.spring.ai.core.enterprise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** In-memory tenant registry for demos and tests. */
public class InMemoryTenantRegistry implements TenantRegistry {
    private final ConcurrentMap<String, TenantProfile> tenants = new ConcurrentHashMap<String, TenantProfile>();

    public void save(TenantProfile profile) {
        if (profile != null && profile.getTenantId() != null) {
            tenants.put(profile.getTenantId(), profile);
        }
    }

    public TenantProfile get(String tenantId) {
        return tenants.get(tenantId);
    }

    public List<TenantProfile> list() {
        return Collections.unmodifiableList(new ArrayList<TenantProfile>(tenants.values()));
    }
}
