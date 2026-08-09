package com.c8software.spring.ai.core.idempotency;

import com.c8software.spring.ai.core.execution.ToolResult;

public interface IdempotencyStore {

    ToolResult get(String scope, String key);

    void put(String scope, String key, ToolResult result);
}
