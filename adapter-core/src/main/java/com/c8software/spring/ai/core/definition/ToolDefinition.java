package com.c8software.spring.ai.core.definition;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;

/** Immutable public contract for an AI-callable tool. */
public interface ToolDefinition {
    /** Returns the globally unique tool name. */
    String getName();

    /** Returns the user-facing description. */
    String getDescription();

    /** Returns ordered parameter definitions. */
    List<ToolParameter> getParameters();

    /** Returns tool metadata. */
    ToolMetadata getMetadata();

    /** Returns the target bean instance. */
    Object getTargetBean();

    /** Returns the target method. */
    Method getMethod();

    /** Returns cached method handle when available. */
    MethodHandle getMethodHandle();
}
