package com.c8software.spring.ai.core.enterprise;

import java.util.List;

/** Marketplace for versioned prompt assets. */
public interface PromptMarketplace {
    void publish(PromptAsset asset);

    PromptAsset get(String id);

    List<PromptAsset> list();
}
