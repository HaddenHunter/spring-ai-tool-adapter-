package com.c8software.spring.ai.core.enterprise;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.registry.ToolRegistry;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

/** Tool marketplace backed by the current tool registry. */
public class RegistryToolMarketplace implements ToolMarketplace {
    private final ToolRegistry registry;

    public RegistryToolMarketplace(ToolRegistry registry) {
        this.registry = registry;
    }

    public List<ToolMarketplaceItem> list() {
        List<ToolMarketplaceItem> result = new ArrayList<ToolMarketplaceItem>();
        for (ToolDefinition definition : registry.listAll()) {
            result.add(describe(definition));
        }
        return Collections.unmodifiableList(result);
    }

    public ToolMarketplaceItem describe(ToolDefinition definition) {
        LinkedHashSet<String> samples = new LinkedHashSet<String>();
        samples.add("Call " + definition.getName() + " with validated JSON arguments.");
        return new ToolMarketplaceItem(
                definition.getName(),
                definition.getMetadata().getGroup(),
                definition.getMetadata().getVersion(),
                definition.getMetadata().getVisibility(),
                definition.getMetadata().getRiskLevel(),
                definition.getMetadata().getRequiresPermission(),
                samples
        );
    }
}
