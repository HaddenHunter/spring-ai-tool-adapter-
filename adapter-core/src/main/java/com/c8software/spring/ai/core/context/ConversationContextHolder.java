package com.c8software.spring.ai.core.context;

import java.time.Instant;

public final class ConversationContextHolder {

    private static final ThreadLocal<BoundContext> HOLDER = new ThreadLocal<BoundContext>();

    private ConversationContextHolder() {
    }

    public static void bind(ConversationSession session, TaskContext taskContext) {
        HOLDER.set(new BoundContext(session, taskContext));
    }

    public static BoundContext current() {
        return HOLDER.get();
    }

    public static ContextSnapshot snapshot() {
        BoundContext context = HOLDER.get();
        if (context == null) {
            return null;
        }
        return new ContextSnapshot(context.getSession(), context.getTaskContext(), Instant.now());
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static final class BoundContext {
        private final ConversationSession session;
        private final TaskContext taskContext;

        private BoundContext(ConversationSession session, TaskContext taskContext) {
            this.session = session;
            this.taskContext = taskContext;
        }

        public ConversationSession getSession() {
            return session;
        }

        public TaskContext getTaskContext() {
            return taskContext;
        }
    }
}
