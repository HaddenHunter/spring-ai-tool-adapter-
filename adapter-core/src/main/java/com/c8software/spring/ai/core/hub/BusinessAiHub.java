package com.c8software.spring.ai.core.hub;

/** Business AI hub boundary for v1 multi-turn orchestration. */
public interface BusinessAiHub {
    BusinessAiHubResponse handle(BusinessAiHubRequest request);
}
