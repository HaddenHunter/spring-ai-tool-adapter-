package com.c8software.spring.ai.agent;

import java.util.List;

public interface ReviewGate {
    List<ReviewFinding> review(AgentStepRequest request);
}
