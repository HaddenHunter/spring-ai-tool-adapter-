package com.c8software.spring.ai.core.schema;

/** DeepSeek schema converter compatible with OpenAI format. */
public class DeepSeekSchemaConverter extends OpenAIFunctionSchemaConverter {
    public String provider() {
        return "deepseek";
    }
}
