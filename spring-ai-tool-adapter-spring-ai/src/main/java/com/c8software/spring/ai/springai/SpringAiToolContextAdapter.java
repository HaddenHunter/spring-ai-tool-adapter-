package com.c8software.spring.ai.springai;

import com.c8software.spring.ai.core.context.ContextSnapshot;
import com.c8software.spring.ai.core.context.ConversationContextHolder;
import com.c8software.spring.ai.core.execution.ExecutionContext;

import org.springframework.ai.chat.model.ToolContext;

import java.util.LinkedHashMap;
import java.util.Map;

/** Maps adapter session and task context into Spring AI ToolContext. */
public class SpringAiToolContextAdapter {
    public static final String SESSION_ID = "sessionId";
    public static final String TASK_ID = "taskId";
    public static final String TASK_TYPE = "taskType";
    public static final String TASK_STATUS = "taskStatus";
    public static final String CURRENT_STEP = "currentStep";
    public static final String PENDING_APPROVAL = "pendingApproval";
    public static final String FACTS = "facts";
    public static final String USER_OVERRIDES = "userOverrides";
    public static final String MODEL_PROVIDER = "modelProvider";
    public static final String MODEL_NAME = "modelName";

    /** Creates a ToolContext from the thread-bound adapter context. */
    public ToolContext currentToolContext(ExecutionContext executionContext) {
        return toToolContext(ConversationContextHolder.snapshot(), executionContext);
    }

    /** Creates a ToolContext from an explicit adapter snapshot. */
    public ToolContext toToolContext(ContextSnapshot snapshot, ExecutionContext executionContext) {
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        if (executionContext != null) {
            values.put(DefaultSpringAiExecutionContextFactory.CURRENT_USER, executionContext.getCurrentUser());
            values.put(DefaultSpringAiExecutionContextFactory.TENANT_ID, executionContext.getTenantId());
            values.put(DefaultSpringAiExecutionContextFactory.TRACE_ID, executionContext.getTraceId());
            values.put(DefaultSpringAiExecutionContextFactory.PERMISSIONS, executionContext.getPermissions());
        }
        if (snapshot != null) {
            values.put(SESSION_ID, snapshot.getSessionId());
            values.put(DefaultSpringAiExecutionContextFactory.TENANT_ID, snapshot.getTenantId());
            values.put(DefaultSpringAiExecutionContextFactory.CURRENT_USER, snapshot.getUserId());
            values.put(MODEL_PROVIDER, snapshot.getModelProvider());
            values.put(MODEL_NAME, snapshot.getModelName());
            values.put(TASK_ID, snapshot.getTaskId());
            values.put(TASK_TYPE, snapshot.getTaskType());
            values.put(TASK_STATUS, snapshot.getTaskStatus() == null ? null : snapshot.getTaskStatus().name());
            values.put(CURRENT_STEP, snapshot.getCurrentStep());
            values.put(PENDING_APPROVAL, snapshot.isPendingApproval());
            values.put(FACTS, snapshot.getFacts());
            values.put(USER_OVERRIDES, snapshot.getUserOverrides());
        }
        return new ToolContext(values);
    }
}
