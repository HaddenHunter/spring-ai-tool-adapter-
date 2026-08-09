package com.c8software.spring.ai.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares whether a tool should be visible to LLMs. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AiToolVisibility {
    ToolVisibility value() default ToolVisibility.PUBLIC;
}
