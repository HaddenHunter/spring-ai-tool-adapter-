package com.c8software.spring.ai.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AgentStepResult {
    private final boolean success;
    private final boolean waiting;
    private final Object output;
    private final String errorMessage;
    private final List<ReviewFinding> findings;

    private AgentStepResult(boolean success, boolean waiting, Object output,
                            String errorMessage, List<ReviewFinding> findings) {
        this.success = success;
        this.waiting = waiting;
        this.output = output;
        this.errorMessage = errorMessage;
        this.findings = Collections.unmodifiableList(new ArrayList<ReviewFinding>(
                findings == null ? Collections.<ReviewFinding>emptyList() : findings));
    }

    public static AgentStepResult success(Object output) {
        return new AgentStepResult(true, false, output, null, Collections.<ReviewFinding>emptyList());
    }

    public static AgentStepResult waiting(Object output) {
        return new AgentStepResult(false, true, output, null, Collections.<ReviewFinding>emptyList());
    }

    public static AgentStepResult failure(String errorMessage) {
        return new AgentStepResult(false, false, null, errorMessage, Collections.<ReviewFinding>emptyList());
    }

    public static AgentStepResult review(List<ReviewFinding> findings) {
        boolean blocking = false;
        if (findings != null) {
            for (ReviewFinding finding : findings) {
                blocking = blocking || finding.isBlocking();
            }
        }
        return new AgentStepResult(!blocking, false, findings, blocking ? "Review has blocking findings" : null, findings);
    }

    public boolean isSuccess() { return success; }
    public boolean isWaiting() { return waiting; }
    public Object getOutput() { return output; }
    public String getErrorMessage() { return errorMessage; }
    public List<ReviewFinding> getFindings() { return findings; }
}
