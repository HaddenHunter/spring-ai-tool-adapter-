package com.c8software.spring.ai.core.approval;

import com.c8software.spring.ai.core.orchestration.ApprovalRequest;
import com.c8software.spring.ai.core.orchestration.ApprovalResponse;
import com.c8software.spring.ai.core.orchestration.HumanInTheLoop;

public class ManualApprovalRequiredHumanInTheLoop implements HumanInTheLoop {

    public ApprovalResponse requestApproval(ApprovalRequest request) {
        return new ApprovalResponse(false, "APPROVAL_REQUIRED",
                "No HumanInTheLoop implementation is configured for this high-risk tool");
    }
}
