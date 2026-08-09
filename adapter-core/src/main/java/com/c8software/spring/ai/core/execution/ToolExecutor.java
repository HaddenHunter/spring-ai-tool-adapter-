package com.c8software.spring.ai.core.execution;

/** Executes registered tools with JSON arguments. */
public interface ToolExecutor {
    /** Executes a tool. */
    ToolResult execute(String toolName, String jsonArguments, ExecutionContext executionContext);
}
