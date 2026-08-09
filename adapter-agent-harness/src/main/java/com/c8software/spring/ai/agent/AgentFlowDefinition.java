package com.c8software.spring.ai.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AgentFlowDefinition {
    private final String id;
    private final String name;
    private final List<AgentPhase> phases;

    public AgentFlowDefinition(String id, String name, List<AgentPhase> phases) {
        this.id = id;
        this.name = name;
        this.phases = Collections.unmodifiableList(new ArrayList<AgentPhase>(
                phases == null ? Collections.<AgentPhase>emptyList() : phases));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<AgentPhase> getPhases() { return phases; }
}
