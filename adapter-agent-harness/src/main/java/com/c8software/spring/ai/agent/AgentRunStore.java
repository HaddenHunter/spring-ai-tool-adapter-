package com.c8software.spring.ai.agent;

public interface AgentRunStore {
    AgentRunState get(String runId);

    void save(AgentRunState runState);
}
