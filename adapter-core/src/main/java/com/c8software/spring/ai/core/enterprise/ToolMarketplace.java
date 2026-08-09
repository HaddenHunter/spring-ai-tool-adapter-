package com.c8software.spring.ai.core.enterprise;

import com.c8software.spring.ai.core.definition.ToolDefinition;

import java.util.List;

/** Marketplace view for governed tools. */
public interface ToolMarketplace {
    List<ToolMarketplaceItem> list();

    ToolMarketplaceItem describe(ToolDefinition definition);
}
