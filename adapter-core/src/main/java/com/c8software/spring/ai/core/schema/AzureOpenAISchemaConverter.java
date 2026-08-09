package com.c8software.spring.ai.core.schema;

/** Azure OpenAI schema converter compatible with OpenAI tool format. */
public class AzureOpenAISchemaConverter extends OpenAIFunctionSchemaConverter {
    public String provider() {
        return "azure-openai";
    }
}
