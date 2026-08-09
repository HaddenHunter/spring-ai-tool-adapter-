package com.c8software.spring.ai.core.schema;

import com.c8software.spring.ai.core.definition.ToolDefinition;

import java.util.LinkedHashMap;
import java.util.Map;

/** Tongyi Qwen tool schema converter. */
public class TongyiQwenSchemaConverter extends AbstractJsonSchemaConverter {
    public Map<String, Object> convert(ToolDefinition definition) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("name", definition.getName());
        root.put("description", definition.getDescription());
        root.put("parameters", parametersSchema(definition));
        return root;
    }

    public String provider() {
        return "tongyi-qwen";
    }
}
