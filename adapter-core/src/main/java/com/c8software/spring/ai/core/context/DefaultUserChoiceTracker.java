package com.c8software.spring.ai.core.context;

import com.c8software.spring.ai.core.exception.AiToolExecutionException;

import java.time.Instant;

public class DefaultUserChoiceTracker implements UserChoiceTracker {

    public ContextFact confirmChoice(TaskContext context, String name, Object value, String source) {
        if (context == null) {
            throw new AiToolExecutionException("AIT_CTX_001", "Task context must not be null");
        }
        ContextFact existing = context.getFact(name);
        if (existing != null && existing.isConfirmed() && !sameValue(existing.getValue(), value)) {
            throw new AiToolExecutionException("AIT_CTX_002", "Confirmed choice is immutable: " + name);
        }
        ContextFact fact = new ContextFact(name, value, true, source, Instant.now());
        context.addFact(fact);
        return fact;
    }

    public boolean hasConfirmedChoice(TaskContext context, String name) {
        return context != null && context.getFact(name) != null && context.getFact(name).isConfirmed();
    }

    public Object confirmedValue(TaskContext context, String name) {
        ContextFact fact = context == null ? null : context.getFact(name);
        return fact == null || !fact.isConfirmed() ? null : fact.getValue();
    }

    private boolean sameValue(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }
}
