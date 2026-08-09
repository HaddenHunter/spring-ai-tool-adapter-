package com.c8software.spring.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentPersistenceTest {
    @Test
    void jdbcRunStorePersistsRunAndCheckpoints() {
        JdbcAgentRunStore store = new JdbcAgentRunStore(new JdbcTemplate(dataSource("runs")), new ObjectMapper());
        store.initializeSchema();

        AgentRunState state = new AgentRunState("run-1", "flow-1", Instant.now());
        state.setStatus(AgentRunStatus.WAITING);
        state.setCurrentPhaseId("phase-1");
        state.setCurrentStepId("approval");
        state.markCompleted("collect");
        state.putAttribute("tenantId", "tenant-a");
        state.addCheckpoint(new AgentCheckpoint("checkpoint-1", "run-1", "phase-1",
                "collect", AgentRunStatus.RUNNING, Instant.now()));
        state.addCheckpoint(new AgentCheckpoint("checkpoint-2", "run-1", "phase-1",
                "approval", AgentRunStatus.WAITING, Instant.now()));

        store.save(state);
        AgentRunState restored = store.get("run-1");

        assertThat(restored).isNotNull();
        assertThat(restored.getStatus()).isEqualTo(AgentRunStatus.WAITING);
        assertThat(restored.getCurrentStepId()).isEqualTo("approval");
        assertThat(restored.getCompletedStepIds()).containsExactly("collect");
        assertThat(restored.getAttributes()).containsEntry("tenantId", "tenant-a");
        assertThat(restored.getCheckpoints()).hasSize(2);
    }

    @Test
    void jdbcArtifactStorePersistsArtifacts() {
        JdbcArtifactStore store = new JdbcArtifactStore(new JdbcTemplate(dataSource("artifacts")), new ObjectMapper());
        store.initializeSchema();

        store.save(new AgentArtifact("artifact-1", "run-1", "step-1", "TOOL",
                Collections.singletonMap("message", "ok"), Instant.now()));

        List<AgentArtifact> artifacts = store.listByRunId("run-1");
        assertThat(artifacts).hasSize(1);
        assertThat(artifacts.get(0).getId()).isEqualTo("artifact-1");
        assertThat(String.valueOf(artifacts.get(0).getContent())).contains("message=ok");
    }

    private DataSource dataSource(String name) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
