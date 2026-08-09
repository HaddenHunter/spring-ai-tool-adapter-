package com.c8software.spring.ai.agent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryAgentRunStore implements AgentRunStore {
    private final ConcurrentMap<String, AgentRunState> runs = new ConcurrentHashMap<String, AgentRunState>();

    public AgentRunState get(String runId) {
        return runs.get(runId);
    }

    public void save(AgentRunState runState) {
        if (runState != null) {
            runs.put(runState.getRunId(), runState);
        }
    }
}
