package com.zifang.util.workflow.bpmn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BPMN 2.0 流程定义。
 * <p>
 * 表示一个完整的业务流程定义，包含流程节点和序列流连接。
 * 用于在BPMN模型和工作流配置之间进行转换。
 * <p>
 * 支持的节点类型：
 * <ul>
 *   <li>startEvent - 开始事件</li>
 *   <li>endEvent - 结束事件</li>
 *   <li>userTask - 用户任务</li>
 *   <li>serviceTask - 服务任务</li>
 *   <li>scriptTask - 脚本任务</li>
 *   <li>exclusiveGateway - 排他网关</li>
 *   <li>parallelGateway - 并行网关</li>
 * </ul>
 *
 * @see BpmnFlowNode
 * @see BpmnSequenceFlow
 * @see BpmnModelConverter
 */
public class BpmnProcess {

    /**
     * 流程唯一标识符
     */
    private String id;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程节点列表
     */
    private List<BpmnFlowNode> nodes;

    /**
     * 序列流列表
     */
    private List<BpmnSequenceFlow> flows;

    /**
     * 默认构造函数，初始化空列表
     */
    public BpmnProcess() {
        this.nodes = new ArrayList<>();
        this.flows = new ArrayList<>();
    }

    /**
     * 全参数构造函数
     *
     * @param id    流程唯一标识符
     * @param name  流程名称
     * @param nodes 流程节点列表
     * @param flows 序列流列表
     */
    public BpmnProcess(String id, String name, List<BpmnFlowNode> nodes, List<BpmnSequenceFlow> flows) {
        this.id = id;
        this.name = name;
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.flows = flows != null ? flows : new ArrayList<>();
    }

    /**
     * 获取流程唯一标识符
     *
     * @return 流程ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置流程唯一标识符
     *
     * @param id 流程ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取流程名称
     *
     * @return 流程名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置流程名称
     *
     * @param name 流程名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取流程节点列表
     *
     * @return 流程节点列表
     */
    public List<BpmnFlowNode> getNodes() {
        return nodes;
    }

    /**
     * 设置流程节点列表
     *
     * @param nodes 流程节点列表
     */
    public void setNodes(List<BpmnFlowNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * 获取序列流列表
     *
     * @return 序列流列表
     */
    public List<BpmnSequenceFlow> getFlows() {
        return flows;
    }

    /**
     * 设置序列流列表
     *
     * @param flows 序列流列表
     */
    public void setFlows(List<BpmnSequenceFlow> flows) {
        this.flows = flows;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "BpmnProcess{id='" + id + "', name='" + name + "', nodes=" + nodes + ", flows=" + flows + "}";
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
        BpmnProcess that = (BpmnProcess) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(nodes, that.nodes) &&
                Objects.equals(flows, that.flows);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(id, name, nodes, flows);
    }
}
