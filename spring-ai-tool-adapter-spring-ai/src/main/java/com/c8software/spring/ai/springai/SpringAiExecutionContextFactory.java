package com.c8software.spring.ai.springai;

import com.c8software.spring.ai.core.execution.ExecutionContext;

import org.springframework.ai.chat.model.ToolContext;

/** Creates adapter execution context from Spring AI tool context. */
public interface SpringAiExecutionContextFactory {
    /** Creates the context used by the governed executor. */
    ExecutionContext create(ToolContext toolContext);
}
