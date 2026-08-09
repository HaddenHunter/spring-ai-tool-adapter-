package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.definition.ToolMetadata;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.registry.ReflectionToolDefinition;
import com.c8software.spring.ai.core.schema.OpenAIFunctionSchemaConverter;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaConverterTest {

    enum Color {
        RED,
        BLUE
    }

    @Test
    void exportsEnumSchema() throws Exception {
        ToolParameter parameter = new ToolParameter(
                "color",
                "color option",
                Color.class,
                Color.class,
                true,
                null,
                null
        );
        ReflectionToolDefinition definition = new ReflectionToolDefinition(
                "paint",
                "paint with color",
                Collections.singletonList(parameter),
                ToolMetadata.builder().build(),
                this,
                getClass().getDeclaredMethod("exportsEnumSchema"),
                null
        );

        Map<String, Object> schema = new OpenAIFunctionSchemaConverter().convert(definition);
        Map<String, Object> function = cast(schema.get("function"));
        Map<String, Object> parameters = cast(function.get("parameters"));
        Map<String, Object> properties = cast(parameters.get("properties"));
        Map<String, Object> color = cast(properties.get("color"));

        assertThat(color.get("enum")).isEqualTo(Arrays.asList("RED", "BLUE"));
    }

    @Test
    void exportsMinAndMaxSchema() throws Exception {
        Parameter parameter = getClass().getDeclaredMethod("bounded", Long.class).getParameters()[0];
        ToolParameter toolParameter = new ToolParameter(
                "amount",
                "amount",
                Long.class,
                Long.class,
                true,
                null,
                null,
                String.valueOf(parameter.getAnnotation(Min.class).value()),
                String.valueOf(parameter.getAnnotation(Max.class).value())
        );
        ReflectionToolDefinition definition = new ReflectionToolDefinition(
                "bounded",
                "bounded amount",
                Collections.singletonList(toolParameter),
                ToolMetadata.builder().build(),
                this,
                getClass().getDeclaredMethod("bounded", Long.class),
                null
        );

        Map<String, Object> schema = new OpenAIFunctionSchemaConverter().convert(definition);
        Map<String, Object> function = cast(schema.get("function"));
        Map<String, Object> parameters = cast(function.get("parameters"));
        Map<String, Object> properties = cast(parameters.get("properties"));
        Map<String, Object> amount = cast(properties.get("amount"));

        assertThat(amount.get("minimum").toString()).isEqualTo("1");
        assertThat(amount.get("maximum").toString()).isEqualTo("99");
    }

    void bounded(@Min(1) @Max(99) Long amount) {
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Object value) {
        return (Map<String, Object>) value;
    }
}
