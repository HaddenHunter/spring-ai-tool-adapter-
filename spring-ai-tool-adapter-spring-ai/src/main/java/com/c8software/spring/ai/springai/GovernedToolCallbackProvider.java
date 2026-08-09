package com.c8software.spring.ai.springai;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.ArrayList;
import java.util.List;

/** Exposes registered governed tools to Spring AI. */
public class GovernedToolCallbackProvider implements ToolCallbackProvider {
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final SpringAiExecutionContextFactory contextFactory;
    private final ObjectMapper objectMapper;

    public GovernedToolCallbackProvider(ToolRegistry toolRegistry, ToolExecutor toolExecutor) {
        this(toolRegistry, toolExecutor, new DefaultSpringAiExecutionContextFactory(), new ObjectMapper());
    }

    public GovernedToolCallbackProvider(ToolRegistry toolRegistry,
                                        ToolExecutor toolExecutor,
                                        SpringAiExecutionContextFactory contextFactory,
                                        ObjectMapper objectMapper) {
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.contextFactory = contextFactory;
        this.objectMapper = objectMapper;
    }

    public ToolCallback[] getToolCallbacks() {
        List<ToolCallback> callbacks = new ArrayList<ToolCallback>();
        for (ToolDefinition definition : toolRegistry.listAll()) {
            callbacks.add(new SpringAiToolCallback(definition, toolExecutor, contextFactory, objectMapper));
        }
        return callbacks.toArray(new ToolCallback[callbacks.size()]);
    }
}
