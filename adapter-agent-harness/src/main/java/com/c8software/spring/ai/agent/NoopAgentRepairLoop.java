package com.c8software.spring.ai.agent;

public class NoopAgentRepairLoop implements AgentRepairLoop {
    public boolean repair(AgentStepRequest request, AgentStepResult failedResult, int attempt) {
        return false;
    }
}
