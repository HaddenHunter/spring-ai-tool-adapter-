package com.c8software.spring.ai.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentFlowSpecParserTest {
    private final AgentFlowSpecParser parser = new JacksonAgentFlowSpecParser();

    @Test
    void parsesJsonFlowSpec() {
        String json = "{"
                + "\"id\":\"refund-flow\","
                + "\"name\":\"Refund Flow\","
                + "\"phases\":[{\"id\":\"collect\",\"name\":\"Collect\",\"steps\":["
                + "{\"id\":\"query-order\",\"name\":\"Query Order\",\"type\":\"tool\",\"toolName\":\"query_order\","
                + "\"arguments\":{\"orderId\":1001},\"maxRepairAttempts\":1}"
                + "]}]}";

        AgentFlowDefinition flow = parser.parseJson(json);

        assertThat(flow.getId()).isEqualTo("refund-flow");
        assertThat(flow.getPhases()).hasSize(1);
        AgentStep step = flow.getPhases().get(0).getSteps().get(0);
        assertThat(step.getType()).isEqualTo(AgentStepType.TOOL);
        assertThat(step.getToolName()).isEqualTo("query_order");
        assertThat(step.getArgumentsJson()).contains("\"orderId\":1001");
        assertThat(step.getMaxRepairAttempts()).isEqualTo(1);
    }

    @Test
    void parsesYamlFlowSpec() {
        String yaml = ""
                + "id: support-flow\n"
                + "name: Support Flow\n"
                + "phases:\n"
                + "  - id: triage\n"
                + "    name: Triage\n"
                + "    steps:\n"
                + "      - id: inspect\n"
                + "        name: Inspect\n"
                + "        type: noop\n"
                + "      - id: review\n"
                + "        name: Review\n"
                + "        type: review\n"
                + "        maxRepairAttempts: 2\n"
                + "      - id: approval\n"
                + "        name: Approval\n"
                + "        type: human\n";

        AgentFlowDefinition flow = parser.parseYaml(yaml);

        assertThat(flow.getId()).isEqualTo("support-flow");
        assertThat(flow.getPhases().get(0).getSteps()).hasSize(3);
        assertThat(flow.getPhases().get(0).getSteps().get(1).getType()).isEqualTo(AgentStepType.REVIEW);
        assertThat(flow.getPhases().get(0).getSteps().get(1).getMaxRepairAttempts()).isEqualTo(2);
        assertThat(flow.getPhases().get(0).getSteps().get(2).getType()).isEqualTo(AgentStepType.HUMAN);
    }

    @Test
    void rejectsSpecWithoutFlowId() {
        assertThatThrownBy(() -> parser.parseJson("{\"name\":\"Missing id\"}"))
                .hasMessageContaining("Flow id must not be empty");
    }
}
