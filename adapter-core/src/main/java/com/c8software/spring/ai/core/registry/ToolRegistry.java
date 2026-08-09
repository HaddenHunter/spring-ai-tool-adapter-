package com.c8software.spring.ai.core.registry;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.exception.AiToolRegistrationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Thread-safe, read-mostly tool registry. */
public class ToolRegistry {
    private final ConcurrentMap<String, ToolDefinition> tools = new ConcurrentHashMap<String, ToolDefinition>();

    /** Registers a globally unique tool. */
    public void register(ToolDefinition definition) {
        if (definition == null || definition.getName() == null || definition.getName().trim().isEmpty()) {
            throw new AiToolRegistrationException("AIT_REG_001", "Tool definition and name must not be empty");
        }
        ToolDefinition previous = tools.putIfAbsent(definition.getName(), definition);
        if (previous != null) {
            throw new AiToolRegistrationException("AIT_REG_002", "Duplicate tool name: " + definition.getName());
        }
    }

    /** Returns a tool by name or null. */
    public ToolDefinition get(String name) {
        return tools.get(name);
    }

    /** Returns all registered tools. */
    public List<ToolDefinition> listAll() {
        return Collections.unmodifiableList(new ArrayList<ToolDefinition>(tools.values()));
    }

    /** Returns tools in a group. */
    public List<ToolDefinition> listByGroup(String group) {
        List<ToolDefinition> result = new ArrayList<ToolDefinition>();
        for (ToolDefinition definition : tools.values()) {
            if (definition.getMetadata() != null && safeEquals(group, definition.getMetadata().getGroup())) {
                result.add(definition);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private boolean safeEquals(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }
}
