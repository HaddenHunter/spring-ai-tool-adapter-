package com.c8software.spring.ai.agent;

public interface AgentRepairLoop {
    boolean repair(AgentStepRequest request, AgentStepResult failedResult, int attempt);
}
