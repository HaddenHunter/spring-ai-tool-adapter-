package com.c8software.spring.ai.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a tool parameter, return value, or field as sensitive for governance metadata. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
public @interface AiToolSensitive {
    SensitiveType type() default SensitiveType.CUSTOM;
}
