package com.c8software.spring.ai.core.context;

public interface UserChoiceTracker {

    ContextFact confirmChoice(TaskContext context, String name, Object value, String source);

    boolean hasConfirmedChoice(TaskContext context, String name);

    Object confirmedValue(TaskContext context, String name);
}
