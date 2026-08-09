package com.c8software.spring.ai.core.approval;

public final class ApprovalDecision {

    private final boolean approved;
    private final String status;
    private final String comment;

    private ApprovalDecision(boolean approved, String status, String comment) {
        this.approved = approved;
        this.status = status;
        this.comment = comment;
    }

    public static ApprovalDecision approved(String comment) {
        return new ApprovalDecision(true, "APPROVED", comment);
    }

    public static ApprovalDecision rejected(String status, String comment) {
        return new ApprovalDecision(false, status == null ? "REJECTED" : status, comment);
    }

    public boolean isApproved() {
        return approved;
    }

    public String getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }
}
