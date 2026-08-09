package com.c8software.spring.ai.core.approval;

import com.c8software.spring.ai.core.definition.ToolDefinition;
import com.c8software.spring.ai.core.execution.ExecutionContext;

public interface ToolApprovalManager {

    ApprovalDecision approve(ToolDefinition definition, ExecutionContext context, String maskedInput);
}
