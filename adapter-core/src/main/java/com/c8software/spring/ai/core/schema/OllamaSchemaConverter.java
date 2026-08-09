package com.c8software.spring.ai.core.schema;

/** Ollama schema converter compatible with OpenAI format. */
public class OllamaSchemaConverter extends OpenAIFunctionSchemaConverter {
    public String provider() {
        return "ollama";
    }
}
