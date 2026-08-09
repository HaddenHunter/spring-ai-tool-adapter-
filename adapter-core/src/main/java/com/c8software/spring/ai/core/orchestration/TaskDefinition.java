package com.c8software.spring.ai.core.orchestration;

import java.util.ArrayList;
import java.util.List;

public class TaskDefinition {

    private final List<TaskNode> nodes = new ArrayList<>();

    private final List<TaskEdge> edges = new ArrayList<>();

    public List<TaskNode> getNodes() {
        return nodes;
    }

    public List<TaskEdge> getEdges() {
        return edges;
    }
}
