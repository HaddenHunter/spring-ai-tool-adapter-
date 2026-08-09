package com.c8software.spring.ai.core.schema;

/** Doubao schema converter compatible with function tool schema. */
public class DoubaoSchemaConverter extends OpenAIFunctionSchemaConverter {
    public String provider() {
        return "doubao";
    }
}
