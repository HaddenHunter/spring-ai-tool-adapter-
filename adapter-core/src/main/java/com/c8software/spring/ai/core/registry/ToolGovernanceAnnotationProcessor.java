package com.c8software.spring.ai.core.registry;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.definition.ToolMetadata;

import java.lang.reflect.Method;

public interface ToolGovernanceAnnotationProcessor {

    ToolMetadata.Builder enrich(Method method, AiTool aiTool, ToolMetadata.Builder builder);
}
