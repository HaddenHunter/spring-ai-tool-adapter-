package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JdbcAgentRunStore implements AgentRunStore {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JdbcAgentRunStore(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper == null ? new ObjectMapper() : objectMapper;
    }

    public void initializeSchema() {
        jdbcTemplate.execute("create table if not exists ai_agent_run ("
                + "run_id varchar(128) primary key,"
                + "flow_id varchar(128),"
                + "status varchar(32),"
                + "current_phase_id varchar(128),"
                + "current_step_id varchar(128),"
                + "error_message varchar(1024),"
                + "completed_step_ids clob,"
                + "attributes_json clob,"
                + "created_at timestamp,"
                + "updated_at timestamp)");
        jdbcTemplate.execute("create table if not exists ai_agent_checkpoint ("
                + "id varchar(128) primary key,"
                + "run_id varchar(128),"
                + "phase_id varchar(128),"
                + "step_id varchar(128),"
                + "status varchar(32),"
                + "created_at timestamp)");
    }

    public AgentRunState get(String runId) {
        List<AgentRunState> states = jdbcTemplate.query("select run_id, flow_id, status, current_phase_id, current_step_id, "
                + "error_message, completed_step_ids, attributes_json, created_at, updated_at from ai_agent_run where run_id = ?",
                runMapper(), runId);
        if (states.isEmpty()) {
            return null;
        }
        AgentRunState state = states.get(0);
        for (AgentCheckpoint checkpoint : checkpoints(runId)) {
            state.addCheckpoint(checkpoint);
        }
        return state;
    }

    public void save(AgentRunState runState) {
        if (runState == null) {
            return;
        }
        jdbcTemplate.update("delete from ai_agent_run where run_id = ?", runState.getRunId());
        jdbcTemplate.update("insert into ai_agent_run "
                        + "(run_id, flow_id, status, current_phase_id, current_step_id, error_message, completed_step_ids, "
                        + "attributes_json, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                runState.getRunId(),
                runState.getFlowId(),
                runState.getStatus().name(),
                runState.getCurrentPhaseId(),
                runState.getCurrentStepId(),
                runState.getErrorMessage(),
                toJson(runState.getCompletedStepIds()),
                toJson(runState.getAttributes()),
                Timestamp.from(runState.getCreatedAt()),
                Timestamp.from(runState.getUpdatedAt()));
        jdbcTemplate.update("delete from ai_agent_checkpoint where run_id = ?", runState.getRunId());
        for (AgentCheckpoint checkpoint : runState.getCheckpoints()) {
            jdbcTemplate.update("insert into ai_agent_checkpoint "
                            + "(id, run_id, phase_id, step_id, status, created_at) values (?, ?, ?, ?, ?, ?)",
                    checkpoint.getId(),
                    checkpoint.getRunId(),
                    checkpoint.getPhaseId(),
                    checkpoint.getStepId(),
                    checkpoint.getStatus().name(),
                    Timestamp.from(checkpoint.getCreatedAt()));
        }
    }

    private List<AgentCheckpoint> checkpoints(String runId) {
        return jdbcTemplate.query("select id, run_id, phase_id, step_id, status, created_at "
                + "from ai_agent_checkpoint where run_id = ? order by created_at asc", checkpointMapper(), runId);
    }

    private RowMapper<AgentRunState> runMapper() {
        return new RowMapper<AgentRunState>() {
            public AgentRunState mapRow(ResultSet rs, int rowNum) throws SQLException {
                AgentRunState state = new AgentRunState(
                        rs.getString("run_id"),
                        rs.getString("flow_id"),
                        rs.getTimestamp("created_at").toInstant());
                state.setStatus(AgentRunStatus.valueOf(rs.getString("status")));
                state.setCurrentPhaseId(rs.getString("current_phase_id"));
                state.setCurrentStepId(rs.getString("current_step_id"));
                state.setErrorMessage(rs.getString("error_message"));
                for (String stepId : completedStepIds(rs.getString("completed_step_ids"))) {
                    state.markCompleted(stepId);
                }
                for (Map.Entry<String, Object> entry : attributes(rs.getString("attributes_json")).entrySet()) {
                    state.putAttribute(entry.getKey(), entry.getValue());
                }
                return state;
            }
        };
    }

    private RowMapper<AgentCheckpoint> checkpointMapper() {
        return new RowMapper<AgentCheckpoint>() {
            public AgentCheckpoint mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new AgentCheckpoint(
                        rs.getString("id"),
                        rs.getString("run_id"),
                        rs.getString("phase_id"),
                        rs.getString("step_id"),
                        AgentRunStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("created_at").toInstant());
            }
        };
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new AiToolExecutionException("AIT_AGENT_RUN_JSON", "Failed to serialize agent run", ex);
        }
    }

    private Set<String> completedStepIds(String value) {
        try {
            List<String> values = value == null ? new ArrayList<String>()
                    : objectMapper.readValue(value, new TypeReference<List<String>>() {});
            return new LinkedHashSet<String>(values);
        } catch (Exception ex) {
            throw new AiToolExecutionException("AIT_AGENT_RUN_JSON", "Failed to deserialize completed steps", ex);
        }
    }

    private Map<String, Object> attributes(String value) {
        try {
            return value == null ? new LinkedHashMap<String, Object>()
                    : objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new AiToolExecutionException("AIT_AGENT_RUN_JSON", "Failed to deserialize attributes", ex);
        }
    }
}
