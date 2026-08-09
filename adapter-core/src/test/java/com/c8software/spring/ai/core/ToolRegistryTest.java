package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.definition.ToolMetadata;
import com.c8software.spring.ai.core.exception.AiToolRegistrationException;
import com.c8software.spring.ai.core.registry.ReflectionToolDefinition;
import com.c8software.spring.ai.core.registry.ToolRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToolRegistryTest {

    @Test
    void rejectsDuplicateNames() throws Exception {
        ToolRegistry registry = new ToolRegistry();
        ReflectionToolDefinition definition = new ReflectionToolDefinition(
                "same",
                "same tool",
                Collections.emptyList(),
                ToolMetadata.builder().group("demo").build(),
                this,
                getClass().getDeclaredMethod("rejectsDuplicateNames"),
                null
        );

        registry.register(definition);

        assertThatThrownBy(() -> registry.register(definition))
                .isInstanceOf(AiToolRegistrationException.class)
                .hasMessageContaining("Duplicate tool name");
        assertThat(registry.listByGroup("demo")).hasSize(1);
    }
}
