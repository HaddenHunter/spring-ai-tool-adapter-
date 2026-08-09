package com.c8software.spring.ai.core.execution;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.exception.AiToolExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Invokes tool methods in a worker thread and releases the caller on timeout. */
public class TimeoutToolInvocationExecutor implements ToolInvocationExecutor {
    private final ExecutorService executorService;

    public TimeoutToolInvocationExecutor() {
        this(Executors.newCachedThreadPool(new DaemonThreadFactory()));
    }

    public TimeoutToolInvocationExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Object invoke(final ToolDefinition definition, final Object[] args) throws Throwable {
        Future<Object> future = executorService.submit(new Callable<Object>() {
            public Object call() throws Exception {
                try {
                    return definition.getMethodHandle().bindTo(definition.getTargetBean()).invokeWithArguments(args);
                } catch (Throwable ex) {
                    if (ex instanceof Exception) {
                        throw (Exception) ex;
                    }
                    throw new ToolInvocationError(ex);
                }
            }
        });
        long timeoutMillis = definition.getMetadata().getTimeoutMillis();
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new AiToolExecutionException("AIT_EXEC_TIMEOUT",
                    "Tool execution timed out after " + timeoutMillis + " ms: " + definition.getName(), ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ToolInvocationError) {
                throw ((ToolInvocationError) cause).getCause();
            }
            if (cause instanceof InvocationTargetException) {
                throw ((InvocationTargetException) cause).getTargetException();
            }
            throw cause;
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private int index;

        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "ai-tool-exec-" + (++index));
            thread.setDaemon(true);
            return thread;
        }
    }

    private static final class ToolInvocationError extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private ToolInvocationError(Throwable cause) {
            super(cause);
        }
    }
}
