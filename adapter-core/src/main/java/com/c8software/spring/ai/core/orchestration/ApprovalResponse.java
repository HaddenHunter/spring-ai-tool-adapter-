package com.c8software.spring.ai.core.orchestration;

public class ApprovalResponse {

    private boolean approved;

    private String status;

    private String comment;

    public ApprovalResponse() {
    }

    public ApprovalResponse(boolean approved, String status, String comment) {
        this.approved = approved;
        this.status = status;
        this.comment = comment;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
