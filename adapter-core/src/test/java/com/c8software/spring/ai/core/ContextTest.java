package com.c8software.spring.ai.core;

import com.c8software.spring.ai.core.context.ContextFact;
import com.c8software.spring.ai.core.context.DefaultContextCompressor;
import com.c8software.spring.ai.core.context.DefaultUserChoiceTracker;
import com.c8software.spring.ai.core.context.TaskContext;
import com.c8software.spring.ai.core.exception.AiToolExecutionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContextTest {

    @Test
    void confirmedChoiceBecomesImmutableFact() {
        TaskContext context = new TaskContext();
        DefaultUserChoiceTracker tracker = new DefaultUserChoiceTracker();

        ContextFact fact = tracker.confirmChoice(context, "selectedCustomerId", "1001", "user-confirmed");

        assertThat(fact.isConfirmed()).isTrue();
        assertThat(tracker.hasConfirmedChoice(context, "selectedCustomerId")).isTrue();
        assertThat(tracker.confirmedValue(context, "selectedCustomerId")).isEqualTo("1001");
        assertThatThrownBy(() -> tracker.confirmChoice(context, "selectedCustomerId", "2002", "user-confirmed"))
                .isInstanceOf(AiToolExecutionException.class)
                .hasMessageContaining("immutable");
    }

    @Test
    void compressionRetainsConfirmedFactsAndLimitsUtterances() {
        TaskContext context = new TaskContext();
        DefaultUserChoiceTracker tracker = new DefaultUserChoiceTracker();
        tracker.confirmChoice(context, "selectedTemplateId", "B", "user-confirmed");
        context.addUserUtterance("one");
        context.addUserUtterance("two");
        context.addUserUtterance("three");

        new DefaultContextCompressor(2).compress(context);

        assertThat(context.isCompressed()).isTrue();
        assertThat(context.getFact("selectedTemplateId").getValue()).isEqualTo("B");
        assertThat(context.getRecentUserUtterances()).containsExactly("two", "three");
    }
}
