package com.c8software.spring.ai.core.idempotency;

import com.c8software.spring.ai.core.execution.ToolResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final ConcurrentMap<String, ToolResult> results = new ConcurrentHashMap<String, ToolResult>();

    public ToolResult get(String scope, String key) {
        return results.get(scope + ":" + key);
    }

    public void put(String scope, String key, ToolResult result) {
        if (result != null) {
            results.put(scope + ":" + key, result);
        }
    }
}
