package com.zifang.util.workflow.engine.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流执行结果。
 * 包含执行状态、已完成节点、待处理节点、运行时变量等信息。
 * 使用Builder模式构建实例。
 *
 * @see ExecutionResult#builder()
 */
public class ExecutionResult {

    /**
     * Process instance identifier
     */
    private String processId;

    /**
     * Current status: RUNNING, COMPLETED, FAILED, PAUSED
     */
    private String status;

    /**
     * List of completed node IDs
     */
    private List<String> completedNodes;

    /**
     * List of pending node IDs
     */
    private List<String> pendingNodes;

    /**
     * List of pending user task node IDs
     */
    private List<String> pendingUserTaskNodes;

    /**
     * Runtime variables
     */
    private Map<String, Object> variables;

    /**
     * Current executing node ID
     */
    private String currentNodeId;

    /**
     * Error message if execution failed
     */
    private String errorMessage;

    /**
     * ExecutionResult方法。
     */
    public ExecutionResult() {
        this.completedNodes = new ArrayList<>();
        this.pendingNodes = new ArrayList<>();
        this.pendingUserTaskNodes = new ArrayList<>();
    }

    /**
     * ExecutionResult方法。
     * * @param processId String类型参数
     *
     * @param status String类型参数
     */
    public ExecutionResult(String processId, String status) {
        this();
        this.processId = processId;
        this.status = status;
    }

    // -------- Builder pattern --------

    /**
     * 使用Builder模式创建执行结果实例。
     *
     * @return ExecutionResultBuilder构建器
     */
    public static ExecutionResultBuilder builder() {
        return new ExecutionResultBuilder();
    }

    /**
     * getProcessId方法。
     *
     * @return String类型返回值
     */
    public String getProcessId() {
        return processId;
    }

    // -------- Getters and Setters --------

    /**
     * setProcessId方法。
     * * @param processId String类型参数
     */
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    /**
     * getStatus方法。
     *
     * @return String类型返回值
     */
    public String getStatus() {
        return status;
    }

    /**
     * setStatus方法。
     * * @param status String类型参数
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * getCompletedNodes方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getCompletedNodes() {
        return completedNodes;
    }

    /**
     * setCompletedNodes方法。
     * * @param completedNodes ListString类型参数
     */
    public void setCompletedNodes(List<String> completedNodes) {
        this.completedNodes = completedNodes;
    }

    /**
     * getPendingNodes方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getPendingNodes() {
        return pendingNodes;
    }

    /**
     * setPendingNodes方法。
     * * @param pendingNodes ListString类型参数
     */
    public void setPendingNodes(List<String> pendingNodes) {
        this.pendingNodes = pendingNodes;
    }

    /**
     * getPendingUserTaskNodes方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getPendingUserTaskNodes() {
        return pendingUserTaskNodes;
    }

    /**
     * setPendingUserTaskNodes方法。
     * * @param pendingUserTaskNodes ListString类型参数
     */
    public void setPendingUserTaskNodes(List<String> pendingUserTaskNodes) {
        this.pendingUserTaskNodes = pendingUserTaskNodes;
    }

    /**
     * getVariables方法。
     *
     * @return Map<String, Object>类型返回值
     */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * setVariables方法。
     * * @param variables MapString,类型参数
     */
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    /**
     * getCurrentNodeId方法。
     *
     * @return String类型返回值
     */
    public String getCurrentNodeId() {
        return currentNodeId;
    }

    /**
     * setCurrentNodeId方法。
     * * @param currentNodeId String类型参数
     */
    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    /**
     * getErrorMessage方法。
     *
     * @return String类型返回值
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * setErrorMessage方法。
     * * @param errorMessage String类型参数
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * addCompletedNode方法。
     * * @param nodeId String类型参数
     */
    public void addCompletedNode(String nodeId) {
        if (!this.completedNodes.contains(nodeId)) {
            this.completedNodes.add(nodeId);
        }
    }

    // -------- Convenience methods --------

    /**
     * addPendingNode方法。
     * * @param nodeId String类型参数
     */
    public void addPendingNode(String nodeId) {
        if (!this.pendingNodes.contains(nodeId)) {
            this.pendingNodes.add(nodeId);
        }
    }

    /**
     * removePendingNode方法。
     * * @param nodeId String类型参数
     */
    public void removePendingNode(String nodeId) {
        this.pendingNodes.remove(nodeId);
    }

    /**
     * addPendingUserTaskNode方法。
     * * @param nodeId String类型参数
     */
    public void addPendingUserTaskNode(String nodeId) {
        if (!this.pendingUserTaskNodes.contains(nodeId)) {
            this.pendingUserTaskNodes.add(nodeId);
        }
    }

    /**
     * removePendingUserTaskNode方法。
     * * @param nodeId String类型参数
     */
    public void removePendingUserTaskNode(String nodeId) {
        this.pendingUserTaskNodes.remove(nodeId);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ExecutionResult{" +
                "processId='" + processId + '\'' +
                ", status='" + status + '\'' +
                ", completedNodes=" + completedNodes +
                ", pendingNodes=" + pendingNodes +
                ", pendingUserTaskNodes=" + pendingUserTaskNodes +
                ", variables=" + variables +
                ", currentNodeId='" + currentNodeId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionResult that = (ExecutionResult) o;
        return Objects.equals(processId, that.processId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(completedNodes, that.completedNodes) &&
                Objects.equals(pendingNodes, that.pendingNodes) &&
                Objects.equals(pendingUserTaskNodes, that.pendingUserTaskNodes) &&
                Objects.equals(variables, that.variables) &&
                Objects.equals(currentNodeId, that.currentNodeId) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(processId, status, completedNodes, pendingNodes, pendingUserTaskNodes, variables, currentNodeId, errorMessage);
    }

    public static class ExecutionResultBuilder {
        private final ExecutionResult result = new ExecutionResult();

        /**
         * processId方法。
         * * @param processId String类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder processId(String processId) {
            result.processId = processId;
            return this;
        }

        /**
         * status方法。
         * * @param status String类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder status(String status) {
            result.status = status;
            return this;
        }

        /**
         * completedNodes方法。
         * * @param completedNodes ListString类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder completedNodes(List<String> completedNodes) {
            result.completedNodes = completedNodes;
            return this;
        }

        /**
         * pendingNodes方法。
         * * @param pendingNodes ListString类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder pendingNodes(List<String> pendingNodes) {
            result.pendingNodes = pendingNodes;
            return this;
        }

        /**
         * pendingUserTaskNodes方法。
         * * @param pendingUserTaskNodes ListString类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder pendingUserTaskNodes(List<String> pendingUserTaskNodes) {
            result.pendingUserTaskNodes = pendingUserTaskNodes;
            return this;
        }

        /**
         * variables方法。
         * * @param variables MapString,类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder variables(Map<String, Object> variables) {
            result.variables = variables;
            return this;
        }

        /**
         * currentNodeId方法。
         * * @param currentNodeId String类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder currentNodeId(String currentNodeId) {
            result.currentNodeId = currentNodeId;
            return this;
        }

        /**
         * errorMessage方法。
         * * @param errorMessage String类型参数
         *
         * @return ExecutionResultBuilder类型返回值
         */
        public ExecutionResultBuilder errorMessage(String errorMessage) {
            result.errorMessage = errorMessage;
            return this;
        }

        /**
         * build方法。
         *
         * @return ExecutionResult类型返回值
         */
        public ExecutionResult build() {
            return result;
        }
    }
}