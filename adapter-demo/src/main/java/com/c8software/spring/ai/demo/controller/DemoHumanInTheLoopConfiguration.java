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
            String toolName = toolName(request == null ? "" : request.getTitle());
            if (!toolName.isEmpty() && approvals.consumeToolApproval(toolName)) {
                return new ApprovalResponse(true, "APPROVED", "approved by demo governance panel");
            }
            return new ApprovalResponse(false, "PENDING_APPROVAL",
                    "waiting for approval in the demo governance panel");
        }

        private String toolName(String title) {
            String prefix = "Approve AI tool call: ";
            if (title != null && title.startsWith(prefix)) {
                return title.substring(prefix.length()).trim();
            }
            return "";
        }
    }
}
