package com.c8software.spring.ai.core.orchestration;

public class TaskEdge {

    private String from;

    private String to;

    public TaskEdge() {
    }

    public TaskEdge(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
