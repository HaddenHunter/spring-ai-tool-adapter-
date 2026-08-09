package com.c8software.spring.ai.core.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcAuditLogger implements AuditLogger {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAuditLogger(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void initializeSchema() {
        jdbcTemplate.execute("create table if not exists ai_tool_audit_log ("
                + "trace_id varchar(128),"
                + "tool_name varchar(128),"
                + "caller_user varchar(128),"
                + "tenant_id varchar(128),"
                + "input_hash varchar(128),"
                + "output_hash varchar(128),"
                + "cost_ms bigint,"
                + "status varchar(32),"
                + "error_message varchar(1024),"
                + "event_type varchar(64),"
                + "context_before_hash varchar(128),"
                + "context_after_hash varchar(128),"
                + "created_at timestamp)");
    }

    public void log(AuditRecord record) {
        jdbcTemplate.update("insert into ai_tool_audit_log "
                        + "(trace_id, tool_name, caller_user, tenant_id, input_hash, output_hash, cost_ms, status, "
                        + "error_message, event_type, context_before_hash, context_after_hash, created_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                record.getTraceId(),
                record.getToolName(),
                record.getCallerUser(),
                record.getTenantId(),
                record.getInputHash(),
                record.getOutputHash(),
                record.getCostMs(),
                record.getStatus(),
                record.getErrorMessage(),
                record.getEventType(),
                record.getContextBeforeHash(),
                record.getContextAfterHash(),
                Timestamp.from(record.getTimestamp()));
    }

    public List<AuditRecord> recent() {
        return query(new AuditQuery(null, null, null, null, null, 100));
    }

    public List<AuditRecord> query(AuditQuery query) {
        StringBuilder sql = new StringBuilder("select trace_id, tool_name, caller_user, tenant_id, input_hash, output_hash, "
                + "cost_ms, status, error_message, event_type, context_before_hash, context_after_hash, created_at "
                + "from ai_tool_audit_log where 1=1");
        List<Object> args = new ArrayList<Object>();
        append(sql, args, "trace_id", query.getTraceId());
        append(sql, args, "tool_name", query.getToolName());
        append(sql, args, "caller_user", query.getCallerUser());
        append(sql, args, "tenant_id", query.getTenantId());
        append(sql, args, "status", query.getStatus());
        sql.append(" order by created_at desc limit ").append(query.getLimit());
        return jdbcTemplate.query(sql.toString(), mapper(), args.toArray());
    }

    private void append(StringBuilder sql, List<Object> args, String column, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sql.append(" and ").append(column).append(" = ?");
            args.add(value);
        }
    }

    private RowMapper<AuditRecord> mapper() {
        return new RowMapper<AuditRecord>() {
            public AuditRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new AuditRecord(
                        rs.getString("trace_id"),
                        rs.getString("tool_name"),
                        rs.getString("caller_user"),
                        rs.getString("tenant_id"),
                        rs.getString("input_hash"),
                        rs.getString("output_hash"),
                        rs.getLong("cost_ms"),
                        rs.getString("status"),
                        rs.getString("error_message"),
                        rs.getString("event_type"),
                        rs.getString("context_before_hash"),
                        rs.getString("context_after_hash"),
                        rs.getTimestamp("created_at").toInstant());
            }
        };
    }
}
