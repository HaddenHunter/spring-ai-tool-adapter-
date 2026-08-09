package com.c8software.spring.ai.agent;

public class ReviewAgentStepExecutor implements AgentStepExecutor {
    private final ReviewGate reviewGate;

    public ReviewAgentStepExecutor(ReviewGate reviewGate) {
        this.reviewGate = reviewGate;
    }

    public boolean supports(AgentStep step) {
        return step != null && AgentStepType.REVIEW.equals(step.getType());
    }

    public AgentStepResult execute(AgentStepRequest request) {
        return AgentStepResult.review(reviewGate.review(request));
    }
}
