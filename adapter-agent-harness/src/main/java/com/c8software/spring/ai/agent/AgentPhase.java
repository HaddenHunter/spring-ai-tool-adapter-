package com.c8software.spring.ai.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AgentPhase {
    private final String id;
    private final String name;
    private final List<AgentStep> steps;

    public AgentPhase(String id, String name, List<AgentStep> steps) {
        this.id = id;
        this.name = name;
        this.steps = Collections.unmodifiableList(new ArrayList<AgentStep>(
                steps == null ? Collections.<AgentStep>emptyList() : steps));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<AgentStep> getSteps() { return steps; }
}
