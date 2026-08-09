package com.c8software.spring.ai.agent;

import java.util.List;

public interface ArtifactStore {
    void save(AgentArtifact artifact);

    List<AgentArtifact> listByRunId(String runId);
}
