package com.c8software.spring.ai.core.audit;

import java.util.List;

/** SPI for non-blocking audit logging. */
public interface AuditLogger {
    /** Logs one record. */
    void log(AuditRecord record);

    /** Returns recent records for demo and tests. */
    List<AuditRecord> recent();

    /** Queries records by optional filters. */
    default List<AuditRecord> query(AuditQuery query) {
        return recent();
    }
}
