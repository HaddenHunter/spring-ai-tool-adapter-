package com.c8software.spring.ai.quickstart;

import com.c8software.spring.ai.core.registry.ToolRegistry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StarterMinimalApplicationTest {

    @Autowired
    private ToolRegistry toolRegistry;

    @Test
    void starterRegistersAnnotatedTool() {
        assertThat(toolRegistry.get("query_account_balance")).isNotNull();
    }
}
