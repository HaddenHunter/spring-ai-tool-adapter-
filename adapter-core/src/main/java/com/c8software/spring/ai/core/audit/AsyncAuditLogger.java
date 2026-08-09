package com.c8software.spring.ai.core.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lightweight asynchronous in-memory audit logger. */
public class AsyncAuditLogger implements AuditLogger {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "ai-tool-audit");
        thread.setDaemon(true);
        return thread;
    });
    private final List<AuditRecord> records = new CopyOnWriteArrayList<AuditRecord>();

    public void log(AuditRecord record) {
        executor.submit(() -> records.add(record));
    }

    public List<AuditRecord> recent() {
        return Collections.unmodifiableList(new ArrayList<AuditRecord>(records));
    }
}
