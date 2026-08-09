package com.c8software.spring.ai.agent;

import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.ArrayList;
import java.util.List;

public class JacksonAgentFlowSpecParser implements AgentFlowSpecParser {
    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;

    public JacksonAgentFlowSpecParser() {
        this(new ObjectMapper(), new ObjectMapper(new YAMLFactory()));
    }

    public JacksonAgentFlowSpecParser(ObjectMapper jsonMapper, ObjectMapper yamlMapper) {
        this.jsonMapper = jsonMapper == null ? new ObjectMapper() : jsonMapper;
        this.yamlMapper = yamlMapper == null ? new ObjectMapper(new YAMLFactory()) : yamlMapper;
    }

    public AgentFlowDefinition parse(String spec, AgentFlowSpecFormat format) {
        if (AgentFlowSpecFormat.YAML.equals(format)) {
            return parseYaml(spec);
        }
        return parseJson(spec);
    }

    public AgentFlowDefinition parseJson(String spec) {
        return parse(spec, jsonMapper);
    }

    public AgentFlowDefinition parseYaml(String spec) {
        return parse(spec, yamlMapper);
    }

    private AgentFlowDefinition parse(String spec, ObjectMapper mapper) {
        try {
            FlowSpec flowSpec = mapper.readValue(spec, FlowSpec.class);
            return convert(flowSpec);
        } catch (AiToolExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AiToolExecutionException("AIT_AGENT_SPEC_PARSE", "Failed to parse agent flow spec", ex);
        }
    }

    private AgentFlowDefinition convert(FlowSpec spec) throws JsonProcessingException {
        if (spec == null || blank(spec.id)) {
            throw new AiToolExecutionException("AIT_AGENT_SPEC_INVALID", "Flow id must not be empty");
        }
        List<AgentPhase> phases = new ArrayList<AgentPhase>();
        if (spec.phases != null) {
            for (PhaseSpec phaseSpec : spec.phases) {
                phases.add(convertPhase(phaseSpec));
            }
        }
        return new AgentFlowDefinition(spec.id, spec.name, phases);
    }

    private AgentPhase convertPhase(PhaseSpec spec) throws JsonProcessingException {
        if (spec == null || blank(spec.id)) {
            throw new AiToolExecutionException("AIT_AGENT_SPEC_INVALID", "Phase id must not be empty");
        }
        List<AgentStep> steps = new ArrayList<AgentStep>();
        if (spec.steps != null) {
            for (StepSpec stepSpec : spec.steps) {
                steps.add(convertStep(stepSpec));
            }
        }
        return new AgentPhase(spec.id, spec.name, steps);
    }

    private AgentStep convertStep(StepSpec spec) throws JsonProcessingException {
        if (spec == null || blank(spec.id)) {
            throw new AiToolExecutionException("AIT_AGENT_SPEC_INVALID", "Step id must not be empty");
        }
        AgentStepType type = spec.type == null ? AgentStepType.NOOP : AgentStepType.valueOf(spec.type.trim().toUpperCase());
        return new AgentStep(spec.id, spec.name, type, spec.toolName, argumentsJson(spec), spec.maxRepairAttempts);
    }

    private String argumentsJson(StepSpec spec) throws JsonProcessingException {
        if (spec.argumentsJson != null) {
            return spec.argumentsJson;
        }
        if (spec.arguments != null) {
            if (spec.arguments instanceof String) {
                return String.valueOf(spec.arguments);
            }
            return jsonMapper.writeValueAsString(spec.arguments);
        }
        return "{}";
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class FlowSpec {
        public String id;
        public String name;
        public List<PhaseSpec> phases;
    }

    public static class PhaseSpec {
        public String id;
        public String name;
        public List<StepSpec> steps;
    }

    public static class StepSpec {
        public String id;
        public String name;
        public String type;
        public String toolName;
        public Object arguments;
        public String argumentsJson;
        public int maxRepairAttempts;
    }
}
