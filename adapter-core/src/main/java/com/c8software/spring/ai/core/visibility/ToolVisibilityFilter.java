package com.c8software.spring.ai.core.visibility;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.execution.ExecutionContext;

import java.util.List;

public interface ToolVisibilityFilter {

    List<ToolDefinition> filter(List<ToolDefinition> definitions, ExecutionContext context);

    boolean isVisible(ToolDefinition definition, ExecutionContext context);
}
