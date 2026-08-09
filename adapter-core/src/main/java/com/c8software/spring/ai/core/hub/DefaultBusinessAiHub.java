package com.c8software.spring.ai.core.hub;

import com.c8software.spring.ai.core.context.ContextSnapshot;
import com.c8software.spring.ai.core.context.ConversationContextHolder;
import com.c8software.spring.ai.core.context.ConversationSession;
import com.c8software.spring.ai.core.context.ConversationSessionStore;
import com.c8software.spring.ai.core.context.DefaultConversationSession;
import com.c8software.spring.ai.core.context.TaskContext;
import com.c8software.spring.ai.core.context.TaskStatus;
import com.c8software.spring.ai.core.execution.ExecutionContext;
import com.c8software.spring.ai.core.execution.ToolExecutor;
import com.c8software.spring.ai.core.execution.ToolResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Default v1 hub that binds session, task context, tool execution, and replay. */
public class DefaultBusinessAiHub implements BusinessAiHub {
    private final ConversationSessionStore sessionStore;
    private final ConversationReplayStore replayStore;
    private final ToolExecutor toolExecutor;

    public DefaultBusinessAiHub(ConversationSessionStore sessionStore, ConversationReplayStore replayStore,
                                ToolExecutor toolExecutor) {
        this.sessionStore = sessionStore;
        this.replayStore = replayStore;
        this.toolExecutor = toolExecutor;
    }

    public BusinessAiHubResponse handle(BusinessAiHubRequest request) {
        ExecutionContext execution = request.getExecutionContext();
        String tenantId = execution == null ? "" : execution.getTenantId();
        String userId = execution == null ? "" : execution.getCurrentUser();
        ConversationSession session = getOrCreateSession(tenantId, userId, request.getSessionId());
        TaskContext task = new TaskContext();
        task.setTaskId(emptyToDefault(request.getTaskId(), UUID.randomUUID().toString()));
        task.setTaskType(emptyToDefault(request.getTaskType(), request.getToolName()));
        task.setTaskStatus(TaskStatus.COLLECTING);
        task.setCurrentStep("user_input");
        task.addUserUtterance(request.getUserInput());

        ConversationContextHolder.bind(session, task);
        ContextSnapshot before = ConversationContextHolder.snapshot();
        ToolResult result = null;
        RuntimeException failure = null;
        try {
            task.setTaskStatus(TaskStatus.EXECUTING);
            task.setCurrentStep("tool:" + request.getToolName());
            result = toolExecutor.execute(request.getToolName(), request.getArgumentsJson(), execution);
            task.setTaskStatus(result.isSuccess() ? TaskStatus.DONE : TaskStatus.FAILED);
            task.setCurrentStep(result.isSuccess() ? "done" : "failed");
            return new BusinessAiHubResponse(session.getId(), task.getTaskId(), task.getTaskStatus().name(),
                    result, ConversationContextHolder.snapshot());
        } catch (RuntimeException ex) {
            task.setTaskStatus(TaskStatus.FAILED);
            task.setCurrentStep("failed");
            failure = ex;
            throw ex;
        } finally {
            ContextSnapshot after = ConversationContextHolder.snapshot();
            replayStore.append(new ConversationTurn(UUID.randomUUID().toString(), session.getId(), tenantId, userId,
                    request.getUserInput(), request.getToolName(), task.getTaskStatus().name(),
                    before, after, attributes(result, failure), Instant.now()));
            session.touch(Instant.now());
            sessionStore.save(session);
            ConversationContextHolder.clear();
        }
    }

    private ConversationSession getOrCreateSession(String tenantId, String userId, String sessionId) {
        String id = emptyToDefault(sessionId, UUID.randomUUID().toString());
        ConversationSession existing = sessionStore.get(tenantId, id);
        if (existing != null) {
            return existing;
        }
        ConversationSession created = new DefaultConversationSession(id, tenantId, userId, "user",
                "demo", "default", Instant.now());
        sessionStore.save(created);
        return created;
    }

    private String emptyToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private Map<String, Object> attributes(ToolResult result, RuntimeException failure) {
        Map<String, Object> attributes = new LinkedHashMap<String, Object>();
        if (result != null) {
            attributes.put("success", result.isSuccess());
            attributes.put("costMs", result.getCostMs());
            attributes.put("errorCode", result.getErrorCode());
        }
        if (failure != null) {
            attributes.put("success", false);
            attributes.put("error", failure.getMessage());
        }
        return attributes;
    }
}
