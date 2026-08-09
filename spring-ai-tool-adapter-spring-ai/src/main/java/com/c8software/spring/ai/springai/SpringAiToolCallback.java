package com.c8software.spring.ai.springai;

import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.execution.ToolResult;
import com.c8software.spring.ai.core.schema.OpenAIFunctionSchemaConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

import java.util.LinkedHashMap;
import java.util.Map;

/** Spring AI ToolCallback backed by the governed adapter executor. */
public class SpringAiToolCallback implements ToolCallback {
    private final com.c8software.spring.ai.core.definition.ToolDefinition adapterDefinition;
    private final ToolExecutor toolExecutor;
    private final SpringAiExecutionContextFactory contextFactory;
    private final ObjectMapper objectMapper;
    private final ToolDefinition toolDefinition;
    private final ToolMetadata toolMetadata;

    public SpringAiToolCallback(com.c8software.spring.ai.core.definition.ToolDefinition adapterDefinition,
                                ToolExecutor toolExecutor,
                                SpringAiExecutionContextFactory contextFactory,
                                ObjectMapper objectMapper) {
        this.adapterDefinition = adapterDefinition;
        this.toolExecutor = toolExecutor;
        this.contextFactory = contextFactory == null ? new DefaultSpringAiExecutionContextFactory() : contextFactory;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        this.toolDefinition = ToolDefinition.builder()
                .name(adapterDefinition.getName())
                .description(adapterDefinition.getDescription())
                .inputSchema(inputSchema(adapterDefinition))
                .build();
        this.toolMetadata = ToolMetadata.builder().returnDirect(false).build();
    }

    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    public ToolMetadata getToolMetadata() {
        return toolMetadata;
    }

    public String call(String toolInput) {
        return call(toolInput, new ToolContext(new LinkedHashMap<String, Object>()));
    }

    public String call(String toolInput, ToolContext toolContext) {
        ExecutionContext executionContext = contextFactory.create(toolContext);
        ToolResult result = toolExecutor.execute(adapterDefinition.getName(), toolInput, executionContext);
        return toJson(result);
    }

    private String inputSchema(com.c8software.spring.ai.core.definition.ToolDefinition definition) {
        Map<String, Object> root = new OpenAIFunctionSchemaConverter().convert(definition);
        Object function = root.get("function");
        if (function instanceof Map) {
            Object parameters = ((Map<?, ?>) function).get("parameters");
            return toJson(parameters);
        }
        return "{}";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize Spring AI tool payload", ex);
        }
    }
}
