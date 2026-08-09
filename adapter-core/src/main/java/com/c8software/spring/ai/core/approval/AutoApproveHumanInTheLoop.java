package com.c8software.spring.ai.core.approval;

import com.c8software.spring.ai.core.orchestration.ApprovalRequest;
import com.c8software.spring.ai.core.orchestration.ApprovalResponse;
import com.c8software.spring.ai.core.orchestration.HumanInTheLoop;

public class AutoApproveHumanInTheLoop implements HumanInTheLoop {

    public ApprovalResponse requestApproval(ApprovalRequest request) {
        return new ApprovalResponse(true, "AUTO_APPROVED", "default local approval");
    }
}
