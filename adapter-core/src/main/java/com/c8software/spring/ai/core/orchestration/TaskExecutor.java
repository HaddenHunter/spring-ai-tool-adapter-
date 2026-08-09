package com.c8software.spring.ai.core.orchestration;

import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.execution.ToolResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskExecutor {

    private final ToolExecutor toolExecutor;

    public TaskExecutor(ToolExecutor toolExecutor) {
        this.toolExecutor = toolExecutor;
    }

    public Map<String, ToolResult> execute(TaskDefinition definition, ExecutionContext context) {
        Map<String, TaskNode> nodes = new LinkedHashMap<>();
        for (TaskNode node : definition.getNodes()) {
            nodes.put(node.getId(), node);
        }

        Map<String, ToolResult> results = new LinkedHashMap<>();
        for (TaskNode node : topologicalSort(definition)) {
            if (node.getType() == TaskNodeType.TOOL) {
                ToolResult result = toolExecutor.execute(node.getToolName(), node.getArgumentsJson(), context);
                results.put(node.getId(), result);
            }
        }
        return results;
    }

    private List<TaskNode> topologicalSort(TaskDefinition definition) {
        Map<String, TaskNode> nodes = new LinkedHashMap<>();
        Map<String, List<String>> outgoing = new HashMap<>();
        for (TaskNode node : definition.getNodes()) {
            nodes.put(node.getId(), node);
            outgoing.put(node.getId(), new ArrayList<>());
        }
        for (TaskEdge edge : definition.getEdges()) {
            outgoing.computeIfAbsent(edge.getFrom(), ignored -> new ArrayList<>()).add(edge.getTo());
        }

        List<TaskNode> ordered = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String id : nodes.keySet()) {
            dfs(id, nodes, outgoing, visiting, visited, ordered);
        }
        return ordered;
    }

    private void dfs(
            String id,
            Map<String, TaskNode> nodes,
            Map<String, List<String>> outgoing,
            Set<String> visiting,
            Set<String> visited,
            List<TaskNode> ordered
    ) {
        if (visited.contains(id)) {
            return;
        }
        if (!visiting.add(id)) {
            throw new AiToolExecutionException("AIT_TASK_CYCLE", "Task graph contains cycle at node: " + id);
        }
        for (String next : outgoing.getOrDefault(id, Collections.emptyList())) {
            dfs(next, nodes, outgoing, visiting, visited, ordered);
        }
        visiting.remove(id);
        visited.add(id);
        TaskNode node = nodes.get(id);
        if (node != null) {
            ordered.add(0, node);
        }
    }
}
