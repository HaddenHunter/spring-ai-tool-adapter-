package com.c8software.spring.ai.core.context;

public class DefaultContextCompressor implements ContextCompressor {

    private final int maxUserUtterances;

    public DefaultContextCompressor() {
        this(5);
    }

    public DefaultContextCompressor(int maxUserUtterances) {
        this.maxUserUtterances = maxUserUtterances;
    }

    public TaskContext compress(TaskContext context) {
        if (context == null) {
            return null;
        }
        context.retainLastUtterances(maxUserUtterances);
        context.setCompressed(true);
        return context;
    }
}
