package com.c8software.spring.ai.core.registry;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.definition.ToolMetadata;
import com.c8software.spring.ai.core.definition.ToolParameter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reflection-backed tool definition with cached MethodHandle. */
public final class ReflectionToolDefinition implements ToolDefinition {
    private final String name;
    private final String description;
    private final List<ToolParameter> parameters;
    private final ToolMetadata metadata;
    private final Object targetBean;
    private final Method method;
    private final MethodHandle methodHandle;

    public ReflectionToolDefinition(String name, String description, List<ToolParameter> parameters,
                                    ToolMetadata metadata, Object targetBean, Method method,
                                    MethodHandle methodHandle) {
        this.name = name;
        this.description = description;
        this.parameters = Collections.unmodifiableList(new ArrayList<ToolParameter>(parameters));
        this.metadata = metadata;
        this.targetBean = targetBean;
        this.method = method;
        this.methodHandle = methodHandle;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<ToolParameter> getParameters() { return parameters; }
    public ToolMetadata getMetadata() { return metadata; }
    public Object getTargetBean() { return targetBean; }
    public Method getMethod() { return method; }
    public MethodHandle getMethodHandle() { return methodHandle; }
}
