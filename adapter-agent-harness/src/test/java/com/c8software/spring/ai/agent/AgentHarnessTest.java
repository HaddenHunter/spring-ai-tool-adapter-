package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.annotation.AiTool;
import com.c8software.spring.ai.core.audit.AsyncAuditLogger;
import com.c8software.spring.ai.core.config.AiToolProperties;
import com.c8software.spring.ai.core.execution.DefaultToolExecutor;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.registry.AiToolRegistrar;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import com.c8software.spring.ai.core.security.DefaultPermissionChecker;
import com.c8software.spring.ai.core.security.DefaultSensitiveMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentHarnessTest {
    @Test
    void runsToolFlowWithCheckpointsAndArtifacts() {
        AgentHarness harness = harness(new PassReviewGate(), new NoopAgentRepairLoop());
        AgentFlowDefinition flow = new AgentFlowDefinition("flow-1", "Tool flow", Collections.singletonList(
                new AgentPhase("phase-1", "Execute", Collections.singletonList(
                        new AgentStep("step-1", "Hello", AgentStepType.TOOL, "hello", "{\"name\":\"Ada\"}", 0)
                ))
        ));

        AgentRunState state = harness.start(flow, context());

        assertThat(state.getStatus()).isEqualTo(AgentRunStatus.COMPLETED);
        assertThat(state.getCompletedStepIds()).containsExactly("step-1");
        assertThat(state.getCheckpoints()).hasSize(1);
        assertThat(state.getArtifacts()).hasSize(1);
        assertThat(state.getArtifacts().get(0).getContent()).isEqualTo("Hello Ada");
    }

    @Test
    void stopsAtHumanStepAndCanResumeAfterMarkingStepComplete() {
        InMemoryAgentRunStore store = new InMemoryAgentRunStore();
        AgentHarness harness = new DefaultAgentHarness(Arrays.<AgentStepExecutor>asList(
                new HumanAgentStepExecutor(), new NoopAgentStepExecutor()), store, new NoopAgentRepairLoop());
        AgentFlowDefinition flow = new AgentFlowDefinition("flow-2", "Human flow", Collections.singletonList(
                new AgentPhase("phase-1", "Approval", Arrays.asList(
                        new AgentStep("approve", "Approve", AgentStepType.HUMAN, null, "{}", 0),
                        new AgentStep("after", "After", AgentStepType.NOOP, null, "{}", 0)
                ))
        ));

        AgentRunState waiting = harness.start(flow, context());
        assertThat(waiting.getStatus()).isEqualTo(AgentRunStatus.WAITING);
        assertThat(waiting.getCurrentStepId()).isEqualTo("approve");

        waiting.markCompleted("approve");
        AgentRunState resumed = harness.resume(waiting.getRunId(), flow, context());
        assertThat(resumed.getStatus()).isEqualTo(AgentRunStatus.COMPLETED);
        assertThat(resumed.getCompletedStepIds()).contains("approve", "after");
    }

    @Test
    void retriesReviewThroughRepairLoop() {
        FlakyReviewGate reviewGate = new FlakyReviewGate();
        CountingRepairLoop repairLoop = new CountingRepairLoop();
        AgentHarness harness = new DefaultAgentHarness(Collections.<AgentStepExecutor>singletonList(
                new ReviewAgentStepExecutor(reviewGate)), new InMemoryAgentRunStore(), repairLoop);
        AgentFlowDefinition flow = new AgentFlowDefinition("flow-3", "Review flow", Collections.singletonList(
                new AgentPhase("phase-1", "Review", Collections.singletonList(
                        new AgentStep("review", "Review", AgentStepType.REVIEW, null, "{}", 1)
                ))
        ));

        AgentRunState state = harness.start(flow, context());

        assertThat(state.getStatus()).isEqualTo(AgentRunStatus.COMPLETED);
        assertThat(reviewGate.calls).isEqualTo(2);
        assertThat(repairLoop.calls).isEqualTo(1);
        assertThat(state.getAttributes()).containsEntry("lastRepairAttempt", 1);
    }

    private AgentHarness harness(ReviewGate reviewGate, AgentRepairLoop repairLoop) {
        ToolRegistry registry = new ToolRegistry();
        new AiToolRegistrar(registry, new AiToolProperties())
                .postProcessAfterInitialization(new DemoTools(), "demoTools");
        DefaultToolExecutor toolExecutor = new DefaultToolExecutor(
                registry,
                new DefaultPermissionChecker(),
                new DefaultSensitiveMasker(),
                new AsyncAuditLogger(),
                new ObjectMapper()
        );
        List<AgentStepExecutor> executors = Arrays.<AgentStepExecutor>asList(
                new ToolAgentStepExecutor(toolExecutor),
                new ReviewAgentStepExecutor(reviewGate),
                new HumanAgentStepExecutor(),
                new NoopAgentStepExecutor()
        );
        return new DefaultAgentHarness(executors, new InMemoryAgentRunStore(), repairLoop);
    }

    private ExecutionContext context() {
        return new ExecutionContext("tester", "tenant", "trace", Collections.emptySet(), Instant.now());
    }

    static class DemoTools {
        @AiTool(name = "hello", description = "Say hello", paramDescriptions = {"name=Name"})
        public String hello(String name) {
            return "Hello " + name;
        }
    }

    static class FlakyReviewGate implements ReviewGate {
        int calls;

        public List<ReviewFinding> review(AgentStepRequest request) {
            calls++;
            if (calls == 1) {
                return Collections.singletonList(new ReviewFinding("HIGH", "Needs repair", true));
            }
            return Collections.emptyList();
        }
    }

    static class CountingRepairLoop implements AgentRepairLoop {
        int calls;

        public boolean repair(AgentStepRequest request, AgentStepResult failedResult, int attempt) {
            calls++;
            return true;
        }
    }
}
