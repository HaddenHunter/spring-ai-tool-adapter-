package com.c8software.spring.ai.agent;

import java.util.Collections;
import java.util.List;

public class PassReviewGate implements ReviewGate {
    public List<ReviewFinding> review(AgentStepRequest request) {
        return Collections.emptyList();
    }
}
