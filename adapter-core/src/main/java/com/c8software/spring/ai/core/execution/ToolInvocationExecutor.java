package com.c8software.spring.ai.core.execution;

import com.c8software.spring.ai.core.definition.ToolDefinition;

/** SPI for invoking business tool methods behind an execution boundary. */
public interface ToolInvocationExecutor {
    Object invoke(ToolDefinition definition, Object[] args) throws Throwable;
}
