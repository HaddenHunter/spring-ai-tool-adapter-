package com.c8software.spring.ai.core.schema;

import com.c8software.spring.ai.core.definition.ToolDefinition;

import java.util.Map;

/** Converts a tool definition to a provider-specific schema. */
public interface ToolSchemaConverter {
    /** Converts one definition. */
    Map<String, Object> convert(ToolDefinition definition);

    /** Returns provider id. */
    String provider();
}
