package com.c8software.spring.ai.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a Spring bean method as an AI-callable tool. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AiTool {
    /** Tool name, globally unique. */
    String name();

    /** Semantic description for the LLM. */
    String description();

    /** Parameter descriptions in method parameter order. */
    String[] paramDescriptions() default {};

    /** Permission expression or permission key. */
    String requiresPermission() default "";

    /** Audit detail level. */
    AuditLevel auditLevel() default AuditLevel.BASIC;

    /** Whether this tool is enabled. */
    boolean enabled() default true;
}
