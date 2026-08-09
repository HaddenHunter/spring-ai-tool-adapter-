package com.c8software.spring.ai.core.definition;

import com.c8software.spring.ai.core.annotation.SensitiveType;

import java.lang.reflect.Type;

/** Immutable definition of one tool parameter. */
public final class ToolParameter {
    private final String name;
    private final String description;
    private final Class<?> type;
    private final Type genericType;
    private final boolean required;
    private final String defaultValue;
    private final SensitiveType sensitiveType;
    private final String minimum;
    private final String maximum;
    private final String contextKey;
    private final boolean contextConfirmed;

    public ToolParameter(String name, String description, Class<?> type, Type genericType,
                         boolean required, String defaultValue, SensitiveType sensitiveType) {
        this(name, description, type, genericType, required, defaultValue, sensitiveType, null, null);
    }

    public ToolParameter(String name, String description, Class<?> type, Type genericType,
                         boolean required, String defaultValue, SensitiveType sensitiveType,
                         String minimum, String maximum) {
        this(name, description, type, genericType, required, defaultValue, sensitiveType, minimum, maximum, null, false);
    }

    public ToolParameter(String name, String description, Class<?> type, Type genericType,
                         boolean required, String defaultValue, SensitiveType sensitiveType,
                         String minimum, String maximum, String contextKey, boolean contextConfirmed) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.genericType = genericType;
        this.required = required;
        this.defaultValue = defaultValue;
        this.sensitiveType = sensitiveType;
        this.minimum = minimum;
        this.maximum = maximum;
        this.contextKey = contextKey;
        this.contextConfirmed = contextConfirmed;
    }

    /** Returns parameter name. */
    public String getName() { return name; }

    /** Returns parameter description. */
    public String getDescription() { return description; }

    /** Returns raw Java type. */
    public Class<?> getType() { return type; }

    /** Returns generic Java type. */
    public Type getGenericType() { return genericType; }

    /** Returns whether this parameter is required. */
    public boolean isRequired() { return required; }

    /** Returns default value. */
    public String getDefaultValue() { return defaultValue; }

    /** Returns sensitive type when present. */
    public SensitiveType getSensitiveType() { return sensitiveType; }

    /** Returns minimum validation value. */
    public String getMinimum() { return minimum; }

    /** Returns maximum validation value. */
    public String getMaximum() { return maximum; }

    /** Returns context key binding when present. */
    public String getContextKey() { return contextKey; }

    /** Returns whether the context binding is a confirmed fact. */
    public boolean isContextConfirmed() { return contextConfirmed; }
}
