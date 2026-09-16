package com.zifang.util.workflow.bpmn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * BPMN流程节点基类。
 * <p>
 * 表示BPMN 2.0中的流程节点，如开始事件、结束事件、用户任务、服务任务、网关等。
 * 包含节点的基本属性和自定义属性存储。
 * <p>
 * 支持的节点类型：
 * <ul>
 *   <li>startEvent - 开始事件</li>
 *   <li>endEvent - 结束事件</li>
 *   <li>userTask - 用户任务</li>
 *   <li>serviceTask - 服务任务</li>
 *   <li>scriptTask - 脚本任务</li>
 *   <li>manualTask - 手工任务</li>
 *   <li>exclusiveGateway - 排他网关</li>
 *   <li>parallelGateway - 并行网关</li>
 *   <li>inclusiveGateway - 包容网关</li>
 * </ul>
 *
 * @see BpmnProcess
 * @see BpmnSequenceFlow
 */
public class BpmnFlowNode {

    /**
     * 节点唯一标识符
     */
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型，如 startEvent、userTask、exclusiveGateway 等
     */
    private String type;

    /**
     * 节点扩展属性
     */
    private Map<String, Object> properties;

    /**
     * 默认构造函数
     */
    public BpmnFlowNode() {
        this.properties = new HashMap<>();
    }

    /**
     * 基础构造函数
     *
     * @param id   节点唯一标识符
     * @param name 节点名称
     * @param type 节点类型
     */
    public BpmnFlowNode(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.properties = new HashMap<>();
    }

    /**
     * 全参数构造函数
     *
     * @param id         节点唯一标识符
     * @param name       节点名称
     * @param type       节点类型
     * @param properties 节点扩展属性
     */
    public BpmnFlowNode(String id, String name, String type, Map<String, Object> properties) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.properties = properties != null ? properties : new HashMap<>();
    }

    /**
     * 获取节点唯一标识符
     *
     * @return 节点ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置节点唯一标识符
     *
     * @param id 节点ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取节点名称
     *
     * @return 节点名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置节点名称
     *
     * @param name 节点名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取节点类型
     *
     * @return 节点类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置节点类型
     *
     * @param type 节点类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取节点扩展属性
     *
     * @return 属性映射表
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * 设置节点扩展属性
     *
     * @param properties 属性映射表
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * 设置单个扩展属性
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    /**
     * 获取单个扩展属性
     *
     * @param key 属性键
     * @return 属性值
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "BpmnFlowNode{id='" + id + "', name='" + name + "', type='" + type + "', properties=" + properties + "}";
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
        BpmnFlowNode that = (BpmnFlowNode) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(id, name, type, properties);
    }
}
