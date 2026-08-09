package com.c8software.spring.ai.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaskContext {

    private String taskId;
    private String taskType;
    private TaskStatus taskStatus = TaskStatus.INIT;
    private String currentStep;
    private boolean pendingApproval;
    private boolean compressed;
    private final Map<String, ContextFact> facts = new LinkedHashMap<String, ContextFact>();
    private final Map<String, Object> userOverrides = new LinkedHashMap<String, Object>();
    private final List<String> recentUserUtterances = new ArrayList<String>();

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus == null ? TaskStatus.INIT : taskStatus;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public boolean isPendingApproval() {
        return pendingApproval;
    }

    public void setPendingApproval(boolean pendingApproval) {
        this.pendingApproval = pendingApproval;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public Map<String, ContextFact> getFacts() {
        return Collections.unmodifiableMap(facts);
    }

    public ContextFact getFact(String name) {
        return facts.get(name);
    }

    public void addFact(ContextFact fact) {
        if (fact != null && fact.getName() != null) {
            facts.put(fact.getName(), fact);
        }
    }

    public Map<String, Object> getUserOverrides() {
        return Collections.unmodifiableMap(userOverrides);
    }

    public void putUserOverride(String key, Object value) {
        if (key != null) {
            userOverrides.put(key, value);
        }
    }

    public List<String> getRecentUserUtterances() {
        return Collections.unmodifiableList(recentUserUtterances);
    }

    public void addUserUtterance(String utterance) {
        if (utterance != null && !utterance.trim().isEmpty()) {
            recentUserUtterances.add(utterance);
        }
    }

    public void retainLastUtterances(int maxCount) {
        if (maxCount < 0) {
            maxCount = 0;
        }
        while (recentUserUtterances.size() > maxCount) {
            recentUserUtterances.remove(0);
        }
    }
}
