package com.c8software.spring.ai.core.security;

import com.c8software.spring.ai.core.definition.ToolDefinition;

/** SPI for masking tool return values before they reach LLMs or audit records. */
public interface ResultMasker {
    Object mask(ToolDefinition definition, Object value);
}
