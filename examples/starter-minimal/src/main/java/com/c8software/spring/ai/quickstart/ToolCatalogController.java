package com.c8software.spring.ai.quickstart;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.schema.OpenAIFunctionSchemaConverter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ToolCatalogController {

    private final ToolRegistry toolRegistry;

    private final OpenAIFunctionSchemaConverter schemaConverter = new OpenAIFunctionSchemaConverter();

    public ToolCatalogController(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @GetMapping("/tools")
    public List<Map<String, Object>> tools() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ToolDefinition definition : toolRegistry.listAll()) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", definition.getName());
            item.put("description", definition.getDescription());
            item.put("riskLevel", definition.getMetadata().getRiskLevel());
            item.put("auditLevel", definition.getMetadata().getAuditLevel());
            item.put("parameters", parameters(definition));
            result.add(item);
        }
        return result;
    }

    @GetMapping("/tools/openai-schema")
    public List<Map<String, Object>> openAiSchema() {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ToolDefinition definition : toolRegistry.listAll()) {
            result.add(schemaConverter.convert(definition));
        }
        return result;
    }

    private List<Map<String, Object>> parameters(ToolDefinition definition) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (ToolParameter parameter : definition.getParameters()) {
            Map<String, Object> item = new LinkedHashMap<String, Object>();
            item.put("name", parameter.getName());
            item.put("type", parameter.getType().getSimpleName());
            item.put("description", parameter.getDescription());
            item.put("required", parameter.isRequired());
            result.add(item);
        }
        return result;
    }
}
