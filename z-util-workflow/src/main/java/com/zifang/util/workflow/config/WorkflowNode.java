package com.zifang.util.workflow.config;

import java.util.HashMap;
import java.util.Objects;

/**
 * 工作流节点定义。
 * <p>
 * 描述业务流节点的最小单元定义，包含节点标识、类型、服务单元、调用参数和连接器信息。
 * <p>
 * 主要属性：
 * <ul>
 *   <li>nodeId - 节点唯一标识号</li>
 *   <li>groupId - 节点组别标识，用于逻辑上归类同种处理单元</li>
 *   <li>type - 节点类型，如startEvent、userTask、exclusiveGateway等</li>
 *   <li>serviceUnit - 处理引擎的服务标识</li>
 *   <li>invokeDynamic - 处理引擎的调用方法名</li>
 *   <li>invokeParameter - 调用参数，可被处理引擎识别和转换</li>
 *   <li>connector - 节点连接器，描述此节点与其他节点的关联情况</li>
 * </ul>
 *
 * @see Connector
 * @see ExecutableWorkflowNode
 */
public class WorkflowNode {

    /**
     * 此节点的唯一标识号
     */
    private String nodeId;

    /**
     * 引入组别概念，逻辑上属于同种的处理单元
     */
    private String groupId;

    /**
     * 此节点的别名
     */
    private String name;

    /**
     * 描述节点的性质
     */
    private String type;

    /**
     * 处理引擎的服务标识
     */
    private String serviceUnit;

    /**
     * 处理引擎的处理实例方法
     */
    private String invokeDynamic;

    /**
     * 可以被处理引擎所识别并且转换的参数合集
     */
    private Object invokeParameter;

    /**
     * 描述此节点与其他节点的关联情况
     */
    private Connector connector;

    private HashMap<String, String> cache;

    /**
     * 默认构造函数
     */
    public WorkflowNode() {
    }

    /**
     * 获取节点唯一标识号
     *
     * @return 节点唯一标识号
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * 设置节点唯一标识号
     *
     * @param nodeId 节点唯一标识号，不能为空
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * 获取节点所属组别标识
     *
     * @return 节点组别标识，用于逻辑上归类同种处理单元
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * 设置节点所属组别标识
     *
     * @param groupId 节点组别标识，用于逻辑上归类同种处理单元
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * 获取节点别名
     *
     * @return 节点别名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置节点别名
     *
     * @param name 节点别名，用于友好展示
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取节点类型
     *
     * @return 节点类型，描述节点的性质
     */
    public String getType() {
        return type;
    }

    /**
     * 设置节点类型
     *
     * @param type 节点类型，用于区分不同性质的节点
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取处理引擎的服务标识
     *
     * @return 服务单元标识
     */
    public String getServiceUnit() {
        return serviceUnit;
    }

    /**
     * 设置处理引擎的服务标识
     *
     * @param serviceUnit 服务单元标识，用于定位具体的处理服务
     */
    public void setServiceUnit(String serviceUnit) {
        this.serviceUnit = serviceUnit;
    }

    /**
     * 获取处理引擎的调用方法名
     *
     * @return 调用方法名，用于动态方法调用
     */
    public String getInvokeDynamic() {
        return invokeDynamic;
    }

    /**
     * 设置处理引擎的调用方法名
     *
     * @param invokeDynamic 调用方法名，用于动态方法调用
     */
    public void setInvokeDynamic(String invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
    }

    /**
     * 获取调用参数
     *
     * @return 调用参数对象，可被处理引擎识别和转换
     */
    public Object getInvokeParameter() {
        return invokeParameter;
    }

    /**
     * 设置调用参数
     *
     * @param invokeParameter 调用参数对象，可被处理引擎识别和转换
     */
    public void setInvokeParameter(Object invokeParameter) {
        this.invokeParameter = invokeParameter;
    }

    /**
     * 获取节点连接器
     *
     * @return 节点连接器，描述此节点与其他节点的关联情况
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * 设置节点连接器
     *
     * @param connector 节点连接器，描述此节点与其他节点的关联情况
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    /**
     * 获取节点缓存
     *
     * @return 节点缓存Map，键值对形式存储
     */
    public HashMap<String, String> getCache() {
        return cache;
    }

    /**
     * 设置节点缓存
     *
     * @param cache 节点缓存Map，键值对形式存储
     */
    public void setCache(HashMap<String, String> cache) {
        this.cache = cache;
    }

    /**
     * 防止重复添加值
     *
     * @param nodeId 待添加的后置节点ID
     */
    public void putPost(String nodeId) {
        if (!connector.getPost().contains(nodeId) && !this.nodeId.equals(nodeId)) {
            connector.getPost().add(nodeId);
        }
    }

    /**
     * 防止重复添加值
     *
     * @param nodeId 待添加的前置节点ID
     */
    public void putPre(String nodeId) {
        if (!connector.getPre().contains(nodeId) && !this.nodeId.equals(nodeId)) {
            connector.getPre().add(nodeId);
        }
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "WorkflowNode{nodeId=" + nodeId + ", groupId=" + groupId + ", name=" + name + ", type=" + type + ", serviceUnit=" + serviceUnit + ", invokeDynamic=" + invokeDynamic + ", invokeParameter=" + invokeParameter + ", connector=" + connector + ", cache=" + cache + "}";
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
        WorkflowNode that = (WorkflowNode) o;
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(serviceUnit, that.serviceUnit) &&
                Objects.equals(invokeDynamic, that.invokeDynamic) &&
                Objects.equals(invokeParameter, that.invokeParameter) &&
                Objects.equals(connector, that.connector) &&
                Objects.equals(cache, that.cache);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(nodeId, groupId, name, type, serviceUnit, invokeDynamic, invokeParameter, connector, cache);
    }
}
