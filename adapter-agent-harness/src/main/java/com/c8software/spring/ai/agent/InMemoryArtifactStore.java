package com.c8software.spring.ai.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryArtifactStore implements ArtifactStore {
    private final List<AgentArtifact> artifacts = new CopyOnWriteArrayList<AgentArtifact>();

    public void save(AgentArtifact artifact) {
        if (artifact != null) {
            artifacts.add(artifact);
        }
    }

    public List<AgentArtifact> listByRunId(String runId) {
        List<AgentArtifact> result = new ArrayList<AgentArtifact>();
        for (AgentArtifact artifact : artifacts) {
            if (runId == null ? artifact.getRunId() == null : runId.equals(artifact.getRunId())) {
                result.add(artifact);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
