package com.c8software.spring.ai.core.schema;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.definition.ToolParameter;
import com.c8software.spring.ai.core.exception.AiToolRegistrationException;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared JSON Schema conversion helpers. */
public abstract class AbstractJsonSchemaConverter implements ToolSchemaConverter {
    protected Map<String, Object> openAiCompatible(ToolDefinition definition) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        Map<String, Object> function = new LinkedHashMap<String, Object>();
        root.put("type", "function");
        root.put("function", function);
        function.put("name", definition.getName());
        function.put("description", definition.getDescription());
        function.put("parameters", parametersSchema(definition));
        return root;
    }

    protected Map<String, Object> parametersSchema(ToolDefinition definition) {
        Map<String, Object> parameters = new LinkedHashMap<String, Object>();
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        List<String> required = new ArrayList<String>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        for (ToolParameter parameter : definition.getParameters()) {
            properties.put(parameter.getName(), parameterSchema(parameter));
            if (parameter.isRequired()) {
                required.add(parameter.getName());
            }
        }
        parameters.put("required", required);
        return parameters;
    }

    protected Map<String, Object> parameterSchema(ToolParameter parameter) {
        Map<String, Object> schema = new LinkedHashMap<String, Object>();
        Class<?> type = parameter.getType();
        if (String.class.equals(type) || Character.class.equals(type) || char.class.equals(type)) {
            schema.put("type", "string");
        } else if (Integer.class.equals(type) || int.class.equals(type) || Long.class.equals(type) || long.class.equals(type)
                || Short.class.equals(type) || short.class.equals(type)) {
            schema.put("type", "integer");
        } else if (BigDecimal.class.equals(type) || Double.class.equals(type) || double.class.equals(type)
                || Float.class.equals(type) || float.class.equals(type)) {
            schema.put("type", "number");
        } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            schema.put("type", "boolean");
        } else if (type.isEnum()) {
            schema.put("type", "string");
            schema.put("enum", enumNames(type));
        } else if (List.class.isAssignableFrom(type)) {
            schema.put("type", "array");
            schema.put("items", listItemSchema(parameter));
        } else {
            throw new AiToolRegistrationException("AIT_SCHEMA_001",
                    "Unsupported parameter type for " + parameter.getName() + ": " + type.getName());
        }
        schema.put("description", parameter.getDescription());
        if (parameter.getDefaultValue() != null) {
            schema.put("default", parameter.getDefaultValue());
        }
        if (parameter.getMinimum() != null) {
            schema.put("minimum", numberOrText(parameter.getMinimum()));
        }
        if (parameter.getMaximum() != null) {
            schema.put("maximum", numberOrText(parameter.getMaximum()));
        }
        return schema;
    }

    private Object numberOrText(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return value;
        }
    }

    private Map<String, Object> listItemSchema(ToolParameter parameter) {
        Map<String, Object> item = new LinkedHashMap<String, Object>();
        if (parameter.getGenericType() instanceof ParameterizedType) {
            java.lang.reflect.Type actual = ((ParameterizedType) parameter.getGenericType()).getActualTypeArguments()[0];
            if (actual instanceof Class && String.class.equals(actual)) {
                item.put("type", "string");
                return item;
            }
        }
        item.put("type", "string");
        return item;
    }

    private List<String> enumNames(Class<?> type) {
        List<String> values = new ArrayList<String>();
        Arrays.stream(type.getEnumConstants()).forEach(item -> values.add(((Enum<?>) item).name()));
        return values;
    }
}
