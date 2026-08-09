package com.c8software.spring.ai.core.schema;

import com.c8software.spring.ai.core.definition.ToolDefinition;

import java.util.Map;

/** OpenAI function calling schema converter. */
public class OpenAIFunctionSchemaConverter extends AbstractJsonSchemaConverter {
    public Map<String, Object> convert(ToolDefinition definition) {
        return openAiCompatible(definition);
    }

    public String provider() {
        return "openai";
    }
}
