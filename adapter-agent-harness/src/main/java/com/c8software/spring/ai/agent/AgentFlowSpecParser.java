package com.c8software.spring.ai.agent;

public interface AgentFlowSpecParser {
    AgentFlowDefinition parse(String spec, AgentFlowSpecFormat format);

    AgentFlowDefinition parseJson(String spec);

    AgentFlowDefinition parseYaml(String spec);
}
