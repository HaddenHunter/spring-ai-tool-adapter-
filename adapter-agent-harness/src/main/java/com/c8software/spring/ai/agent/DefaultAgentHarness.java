package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.c8software.spring.ai.core.execution.ExecutionContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefaultAgentHarness implements AgentHarness {
    private final List<AgentStepExecutor> executors;
    private final AgentRunStore runStore;
    private final AgentRepairLoop repairLoop;

    public DefaultAgentHarness(List<AgentStepExecutor> executors, AgentRunStore runStore, AgentRepairLoop repairLoop) {
        this.executors = new ArrayList<AgentStepExecutor>(executors);
        this.runStore = runStore;
        this.repairLoop = repairLoop == null ? new NoopAgentRepairLoop() : repairLoop;
    }

    public AgentRunState start(AgentFlowDefinition flow, ExecutionContext executionContext) {
        AgentRunState state = new AgentRunState(UUID.randomUUID().toString(), flow.getId(), Instant.now());
        runStore.save(state);
        return run(state, flow, executionContext);
    }

    public AgentRunState resume(String runId, AgentFlowDefinition flow, ExecutionContext executionContext) {
        AgentRunState state = runStore.get(runId);
        if (state == null) {
            throw new AiToolExecutionException("AIT_AGENT_RUN_NOT_FOUND", "Agent run not found: " + runId);
        }
        return run(state, flow, executionContext);
    }

    private AgentRunState run(AgentRunState state, AgentFlowDefinition flow, ExecutionContext executionContext) {
        state.setStatus(AgentRunStatus.RUNNING);
        runStore.save(state);
        for (AgentPhase phase : flow.getPhases()) {
            state.setCurrentPhaseId(phase.getId());
            for (AgentStep step : phase.getSteps()) {
                if (state.getCompletedStepIds().contains(step.getId())) {
                    continue;
                }
                state.setCurrentStepId(step.getId());
                AgentStepRequest request = new AgentStepRequest(flow, phase, step, state, executionContext);
                AgentStepResult result = executeWithRepair(request);
                recordArtifact(state, step, result);
                if (result.isWaiting()) {
                    state.setStatus(AgentRunStatus.WAITING);
                    checkpoint(state, phase, step);
                    runStore.save(state);
                    return state;
                }
                if (!result.isSuccess()) {
                    state.setStatus(AgentRunStatus.FAILED);
                    state.setErrorMessage(result.getErrorMessage());
                    checkpoint(state, phase, step);
                    runStore.save(state);
                    return state;
                }
                state.markCompleted(step.getId());
                checkpoint(state, phase, step);
                runStore.save(state);
            }
        }
        state.setStatus(AgentRunStatus.COMPLETED);
        state.setCurrentStepId(null);
        runStore.save(state);
        return state;
    }

    private AgentStepResult executeWithRepair(AgentStepRequest request) {
        AgentStepResult result = executorFor(request.getStep()).execute(request);
        int attempt = 0;
        while (!result.isSuccess() && !result.isWaiting() && attempt < request.getStep().getMaxRepairAttempts()) {
            attempt++;
            boolean repaired = repairLoop.repair(request, result, attempt);
            request.getRunState().putAttribute("lastRepairAttempt", attempt);
            if (!repaired) {
                return result;
            }
            result = executorFor(request.getStep()).execute(request);
        }
        return result;
    }

    private AgentStepExecutor executorFor(AgentStep step) {
        for (AgentStepExecutor executor : executors) {
            if (executor.supports(step)) {
                return executor;
            }
        }
        throw new AiToolExecutionException("AIT_AGENT_EXECUTOR_NOT_FOUND", "No executor for step: " + step.getId());
    }

    private void recordArtifact(AgentRunState state, AgentStep step, AgentStepResult result) {
        state.addArtifact(new AgentArtifact(UUID.randomUUID().toString(), state.getRunId(), step.getId(),
                step.getType().name(), result.getOutput(), Instant.now()));
    }

    private void checkpoint(AgentRunState state, AgentPhase phase, AgentStep step) {
        state.addCheckpoint(new AgentCheckpoint(UUID.randomUUID().toString(), state.getRunId(), phase.getId(),
                step.getId(), state.getStatus(), Instant.now()));
    }
}
