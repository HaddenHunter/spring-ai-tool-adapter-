package com.c8software.spring.ai.core.enterprise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** In-memory prompt marketplace. */
public class InMemoryPromptMarketplace implements PromptMarketplace {
    private final ConcurrentMap<String, PromptAsset> prompts = new ConcurrentHashMap<String, PromptAsset>();

    public void publish(PromptAsset asset) {
        if (asset != null && asset.getId() != null) {
            prompts.put(asset.getId(), asset);
        }
    }

    public PromptAsset get(String id) {
        return prompts.get(id);
    }

    public List<PromptAsset> list() {
        return Collections.unmodifiableList(new ArrayList<PromptAsset>(prompts.values()));
    }
}
