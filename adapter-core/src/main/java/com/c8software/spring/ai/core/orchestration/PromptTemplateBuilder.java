package com.c8software.spring.ai.core.orchestration;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.registry.ToolRegistry;

public class PromptTemplateBuilder {

    private final ToolRegistry registry;

    public PromptTemplateBuilder(ToolRegistry registry) {
        this.registry = registry;
    }

    public String build(ConversationContext context) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are an enterprise AI assistant.\n");
        builder.append("Session: ").append(context.getSessionId()).append('\n');
        builder.append("Use tools only through the audited adapter.\n");
        builder.append("Ask for human approval before high-risk actions.\n");
        builder.append("Available tools:\n");
        for (ToolDefinition definition : registry.listAll()) {
            builder.append("- ")
                    .append(definition.getName())
                    .append(": ")
                    .append(definition.getDescription())
                    .append('\n');
        }
        return builder.toString();
    }
}
