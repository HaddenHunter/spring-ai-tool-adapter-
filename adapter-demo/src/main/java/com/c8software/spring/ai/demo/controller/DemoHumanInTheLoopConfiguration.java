package com.c8software.spring.ai.demo.controller;

import com.c8software.spring.ai.core.orchestration.ApprovalRequest;
import com.c8software.spring.ai.core.orchestration.ApprovalResponse;
import com.c8software.spring.ai.core.orchestration.HumanInTheLoop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoHumanInTheLoopConfiguration {

    @Bean
    public HumanInTheLoop demoHumanInTheLoop(DemoApprovalStore approvals) {
        return new DemoHumanInTheLoop(approvals);
    }

    private static final class DemoHumanInTheLoop implements HumanInTheLoop {
        private final DemoApprovalStore approvals;

        private DemoHumanInTheLoop(DemoApprovalStore approvals) {
            this.approvals = approvals;
        }

        public ApprovalResponse requestApproval(ApprovalRequest request) {
            String approvalId = approvalId(request == null ? "" : request.getDetail());
            if (!approvalId.isEmpty() && approvals.consumeToolApproval(approvalId)) {
                return new ApprovalResponse(true, "APPROVED", "approved by demo governance panel");
            }
            return new ApprovalResponse(false, "PENDING_APPROVAL",
                    "waiting for approval in the demo governance panel");
        }

        private String approvalId(String detail) {
            String marker = "approvalId=";
            if (detail != null) {
                int start = detail.indexOf(marker);
                if (start >= 0) {
                    int valueStart = start + marker.length();
                    int end = detail.indexOf(',', valueStart);
                    return end < 0 ? detail.substring(valueStart).trim() : detail.substring(valueStart, end).trim();
                }
            }
            return "";
        }
    }
}
