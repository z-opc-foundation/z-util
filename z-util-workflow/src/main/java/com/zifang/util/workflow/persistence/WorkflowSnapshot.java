package com.zifang.util.workflow.persistence;

import java.util.List;
import java.util.Map;

/**
 * Captures the full state of a workflow for persistence and recovery.
 * Used by WorkflowPersistencePlugin implementations to save/restore
 * workflow execution state.
 */
public class WorkflowSnapshot {

    private String processId;
    private String status;
    private String workflowConfigurationJson;
    private List<String> completedNodes;
    private List<String> pendingNodes;
    private List<String> pendingUserTaskNodes;
    private Map<String, Object> variables;
    private String currentNodeId;
    private String errorMessage;
    private long lastUpdatedTime;

    public WorkflowSnapshot() {
        this.lastUpdatedTime = System.currentTimeMillis();
    }

    // -------- Getters and Setters --------

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkflowConfigurationJson() {
        return workflowConfigurationJson;
    }

    public void setWorkflowConfigurationJson(String workflowConfigurationJson) {
        this.workflowConfigurationJson = workflowConfigurationJson;
    }

    public List<String> getCompletedNodes() {
        return completedNodes;
    }

    public void setCompletedNodes(List<String> completedNodes) {
        this.completedNodes = completedNodes;
    }

    public List<String> getPendingNodes() {
        return pendingNodes;
    }

    public void setPendingNodes(List<String> pendingNodes) {
        this.pendingNodes = pendingNodes;
    }

    public List<String> getPendingUserTaskNodes() {
        return pendingUserTaskNodes;
    }

    public void setPendingUserTaskNodes(List<String> pendingUserTaskNodes) {
        this.pendingUserTaskNodes = pendingUserTaskNodes;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
