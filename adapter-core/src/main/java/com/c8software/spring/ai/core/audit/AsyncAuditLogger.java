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

    public List<AuditRecord> query(AuditQuery query) {
        List<AuditRecord> result = new ArrayList<AuditRecord>();
        int limit = query == null ? 100 : query.getLimit();
        for (int i = records.size() - 1; i >= 0 && result.size() < limit; i--) {
            AuditRecord record = records.get(i);
            if (matches(query, record)) {
                result.add(record);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private boolean matches(AuditQuery query, AuditRecord record) {
        if (query == null) {
            return true;
        }
        return match(query.getTraceId(), record.getTraceId())
                && match(query.getToolName(), record.getToolName())
                && match(query.getCallerUser(), record.getCallerUser())
                && match(query.getTenantId(), record.getTenantId())
                && match(query.getStatus(), record.getStatus());
    }

    private boolean match(String expected, String actual) {
        return expected == null || expected.trim().isEmpty() || expected.equals(actual);
    }
}
