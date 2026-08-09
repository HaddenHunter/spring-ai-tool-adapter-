package com.c8software.spring.ai.springai;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory Spring AI chat memory repository aligned with adapter session ids. */
public class AdapterChatMemoryRepository implements ChatMemoryRepository {
    private final Map<String, List<Message>> conversations = new LinkedHashMap<String, List<Message>>();

    public synchronized List<String> findConversationIds() {
        return Collections.unmodifiableList(new ArrayList<String>(conversations.keySet()));
    }

    public synchronized List<Message> findByConversationId(String conversationId) {
        List<Message> messages = conversations.get(conversationId);
        if (messages == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<Message>(messages));
    }

    public synchronized void saveAll(String conversationId, List<Message> messages) {
        conversations.put(conversationId, new ArrayList<Message>(
                messages == null ? Collections.<Message>emptyList() : messages));
    }

    public synchronized void deleteByConversationId(String conversationId) {
        conversations.remove(conversationId);
    }
}
