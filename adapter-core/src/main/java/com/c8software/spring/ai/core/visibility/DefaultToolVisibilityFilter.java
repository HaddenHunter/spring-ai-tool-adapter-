package com.c8software.spring.ai.core.visibility;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.execution.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

public class DefaultToolVisibilityFilter implements ToolVisibilityFilter {

    public List<ToolDefinition> filter(List<ToolDefinition> definitions, ExecutionContext context) {
        List<ToolDefinition> result = new ArrayList<ToolDefinition>();
        if (definitions == null) {
            return result;
        }
        for (ToolDefinition definition : definitions) {
            if (isVisible(definition, context)) {
                result.add(definition);
            }
        }
        return result;
    }

    public boolean isVisible(ToolDefinition definition, ExecutionContext context) {
        if (definition == null || definition.getMetadata() == null) {
            return false;
        }
        String visibility = definition.getMetadata().getVisibility();
        if ("DEPRECATED".equals(visibility)) {
            return false;
        }
        if ("INTERNAL".equals(visibility)) {
            return context != null && context.getPermissions().contains("tool:internal");
        }
        return true;
    }
}
