package com.c8software.spring.ai.core.orchestration;

public class ChatOrchestrator {

    private final PromptTemplateBuilder promptTemplateBuilder;

    public ChatOrchestrator(PromptTemplateBuilder promptTemplateBuilder) {
        this.promptTemplateBuilder = promptTemplateBuilder;
    }

    public String prepare(ConversationContext context, String userInput) {
        context.getMessageHistory().add(userInput);
        return promptTemplateBuilder.build(context);
    }
}
