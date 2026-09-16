package com.zifang.util.workflow.conponents;

import com.zifang.util.workflow.config.ExecutableWorkflowNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工作流任务执行单元。
 * <p>
 * 封装了工作流执行所需的所有信息，包括：
 * <ul>
 *   <li>工作流上下文引用 - 用于访问全局状态和配置</li>
 *   <li>起始节点 - 任务执行的入口点</li>
 *   <li>可执行节点列表 - 所有需要执行的节点</li>
 *   <li>节点ID映射 - 快速查找节点</li>
 * </ul>
 * <p>
 * 任务通过调用 {@link #exec()} 方法开始执行，
 * 将从起始节点开始，按照节点连线顺序执行整个流程。
 *
 * @see ExecutableWorkflowNode
 * @see WorkFlowApplicationContext
 */
public class Task {

    /**
     * 工作流应用上下文引用
     */
    private WorkFlowApplicationContext workFlowApplicationContext;

    /**
     * 起始节点，任务执行的入口点
     */
    private ExecutableWorkflowNode start;

    /**
     * 可执行工作流节点列表
     */
    private List<ExecutableWorkflowNode> executableWorkNodes;

    /**
     * 可执行节点ID到节点的映射表
     */
    private Map<String, ExecutableWorkflowNode> executableWorkNodeIdMap;

    /**
     * 默认构造函数
     */
    public Task() {
    }

    /**
     * 获取工作流应用上下文
     *
     * @return 工作流应用上下文
     */
    public WorkFlowApplicationContext getWorkFlowApplicationContext() {
        return workFlowApplicationContext;
    }

    /**
     * 设置工作流应用上下文
     *
     * @param workFlowApplicationContext 工作流应用上下文
     */
    public void setWorkFlowApplicationContext(WorkFlowApplicationContext workFlowApplicationContext) {
        this.workFlowApplicationContext = workFlowApplicationContext;
    }

    /**
     * 获取起始节点
     *
     * @return 起始节点
     */
    public ExecutableWorkflowNode getStart() {
        return start;
    }

    /**
     * 设置起始节点
     *
     * @param start 起始节点
     */
    public void setStart(ExecutableWorkflowNode start) {
        this.start = start;
    }

    /**
     * 获取可执行工作流节点列表
     *
     * @return 可执行节点列表
     */
    public List<ExecutableWorkflowNode> getExecutableWorkNodes() {
        return executableWorkNodes;
    }

    /**
     * 设置可执行工作流节点列表
     *
     * @param executableWorkNodes 可执行节点列表
     */
    public void setExecutableWorkNodes(List<ExecutableWorkflowNode> executableWorkNodes) {
        this.executableWorkNodes = executableWorkNodes;
    }

    /**
     * 获取节点ID到节点的映射表
     *
     * @return 节点映射表
     */
    public Map<String, ExecutableWorkflowNode> getExecutableWorkNodeIdMap() {
        return executableWorkNodeIdMap;
    }

    /**
     * 设置节点ID到节点的映射表
     *
     * @param executableWorkNodeIdMap 节点映射表
     */
    public void setExecutableWorkNodeIdMap(Map<String, ExecutableWorkflowNode> executableWorkNodeIdMap) {
        this.executableWorkNodeIdMap = executableWorkNodeIdMap;
    }

    /**
     * 执行任务。
     * <p>
     * 从起始节点开始执行整个工作流，
     * 按照节点连线顺序依次执行各节点。
     */
    public void exec() {
        start.exec();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Task{" +
                "workFlowApplicationContext=" + workFlowApplicationContext +
                ", start=" + start +
                ", executableWorkNodes=" + executableWorkNodes +
                ", executableWorkNodeIdMap=" + executableWorkNodeIdMap +
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
        Task task = (Task) o;
        return Objects.equals(workFlowApplicationContext, task.workFlowApplicationContext) &&
                Objects.equals(start, task.start) &&
                Objects.equals(executableWorkNodes, task.executableWorkNodes) &&
                Objects.equals(executableWorkNodeIdMap, task.executableWorkNodeIdMap);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(workFlowApplicationContext, start, executableWorkNodes, executableWorkNodeIdMap);
    }
}
