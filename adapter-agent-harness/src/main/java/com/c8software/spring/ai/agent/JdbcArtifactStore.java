package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class JdbcArtifactStore implements ArtifactStore {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcArtifactStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    public void initializeSchema() {
        jdbcTemplate.execute("create table if not exists ai_agent_artifact ("
                + "id varchar(128) primary key,"
                + "run_id varchar(128),"
                + "step_id varchar(128),"
                + "type varchar(64),"
                + "content_json clob,"
                + "created_at timestamp)");
    }

    public void save(AgentArtifact artifact) {
        if (artifact == null) {
            return;
        }
        jdbcTemplate.update("delete from ai_agent_artifact where id = ?", artifact.getId());
        jdbcTemplate.update("insert into ai_agent_artifact "
                        + "(id, run_id, step_id, type, content_json, created_at) values (?, ?, ?, ?, ?, ?)",
                artifact.getId(),
                artifact.getRunId(),
                artifact.getStepId(),
                artifact.getType(),
                toJson(artifact.getContent()),
                Timestamp.from(artifact.getCreatedAt()));
    }

    public List<AgentArtifact> listByRunId(String runId) {
        return jdbcTemplate.query("select id, run_id, step_id, type, content_json, created_at "
                + "from ai_agent_artifact where run_id = ? order by created_at asc", mapper(), runId);
    }

    private RowMapper<AgentArtifact> mapper() {
        return new RowMapper<AgentArtifact>() {
            public AgentArtifact mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new AgentArtifact(
                        rs.getString("id"),
                        rs.getString("run_id"),
                        rs.getString("step_id"),
                        rs.getString("type"),
                        fromJson(rs.getString("content_json")),
                        rs.getTimestamp("created_at").toInstant());
            }
        };
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new AiToolExecutionException("AIT_AGENT_ARTIFACT_JSON", "Failed to serialize artifact", ex);
        }
    }

    private Object fromJson(String value) {
        try {
            return value == null ? null : objectMapper.readValue(value, Object.class);
        } catch (Exception ex) {
            throw new AiToolExecutionException("AIT_AGENT_ARTIFACT_JSON", "Failed to deserialize artifact", ex);
        }
    }
}
