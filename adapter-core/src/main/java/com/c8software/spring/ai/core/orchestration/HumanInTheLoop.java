package com.c8software.spring.ai.core.orchestration;

public interface HumanInTheLoop {

    ApprovalResponse requestApproval(ApprovalRequest request);
}
