package com.c8software.spring.ai.core.security;

import com.c8software.spring.ai.core.annotation.SensitiveType;
import com.c8software.spring.ai.core.definition.ToolDefinition;

/** Masks a whole return value when the tool method declares result sensitivity. */
public class DefaultResultMasker implements ResultMasker {
    private final SensitiveMasker sensitiveMasker;

    public DefaultResultMasker(SensitiveMasker sensitiveMasker) {
        this.sensitiveMasker = sensitiveMasker;
    }

    public Object mask(ToolDefinition definition, Object value) {
        String type = definition.getMetadata().getResultSensitiveType();
        if (type == null || type.trim().isEmpty()) {
            return value;
        }
        return sensitiveMasker.mask(value, SensitiveType.valueOf(type));
    }
}
