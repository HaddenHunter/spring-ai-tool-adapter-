package com.c8software.spring.ai.demo.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsCollector {

    private final Counter toolCalls;

    private final Counter toolErrors;

    private final Counter tokenUsage;

    private final AtomicInteger activeConversations = new AtomicInteger();

    public MetricsCollector(MeterRegistry registry) {
        this.toolCalls = Counter.builder("tool_call_total").register(registry);
        this.toolErrors = Counter.builder("tool_call_error_total").register(registry);
        this.tokenUsage = Counter.builder("token_usage_total").register(registry);
        registry.gauge("active_conversations", activeConversations);
    }

    public void recordToolCall() {
        toolCalls.increment();
    }

    public void recordToolError() {
        toolErrors.increment();
    }

    public void recordTokenUsage(double tokens) {
        tokenUsage.increment(tokens);
    }

    public AtomicInteger activeConversations() {
        return activeConversations;
    }
}
