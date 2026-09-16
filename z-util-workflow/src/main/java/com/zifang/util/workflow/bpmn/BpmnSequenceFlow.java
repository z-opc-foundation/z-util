package com.zifang.util.workflow.bpmn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * BPMN 2.0 序列流。
 * <p>
 * 表示BPMN流程中连接两个节点的顺序流，用于定义流程的执行路径。
 * 序列流可以带有条件表达式，用于网关节点的条件分支判断。
 * <p>
 * 主要属性：
 * <ul>
 *   <li>sourceRef - 源节点引用</li>
 *   <li>targetRef - 目标节点引用</li>
 *   <li>conditionExpression - 条件表达式（用于条件分支）</li>
 *   <li>isDefault - 是否为默认流向（仅排他网关使用）</li>
 * </ul>
 *
 * @see BpmnProcess
 * @see BpmnFlowNode
 * @see GatewayEvaluator
 */
public class BpmnSequenceFlow {

    private String id;
    private String sourceRef;
    private String targetRef;
    private String name;
    private String conditionExpression;
    private boolean isDefault;
    private Map<String, String> properties = new HashMap<>();

    /**
     * 默认构造函数。
     */
    public BpmnSequenceFlow() {
    }

    /**
     * 使用 ID、源节点和目标节点构造序列流。
     *
     * @param id        序列流唯一标识符
     * @param sourceRef 源节点 ID 引用
     * @param targetRef 目标节点 ID 引用
     */
    public BpmnSequenceFlow(String id, String sourceRef, String targetRef) {
        this.id = id;
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
    }

    /**
     * 使用完整属性构造序列流。
     *
     * @param id                  序列流唯一标识符
     * @param sourceRef           源节点 ID 引用
     * @param targetRef           目标节点 ID 引用
     * @param name                序列流名称
     * @param conditionExpression 条件表达式
     * @param isDefault           是否为默认流向
     */
    public BpmnSequenceFlow(String id, String sourceRef, String targetRef, String name, String conditionExpression, boolean isDefault) {
        this.id = id;
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
        this.name = name;
        this.conditionExpression = conditionExpression;
        this.isDefault = isDefault;
    }

    /**
     * 获取序列流唯一标识符。
     *
     * @return 序列流 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置序列流唯一标识符。
     *
     * @param id 序列流 ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取序列流源节点 ID 引用。
     *
     * @return 源节点 ID
     */
    public String getSourceRef() {
        return sourceRef;
    }

    /**
     * 设置序列流源节点 ID 引用。
     *
     * @param sourceRef 源节点 ID
     */
    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    /**
     * 获取序列流目标节点 ID 引用。
     *
     * @return 目标节点 ID
     */
    public String getTargetRef() {
        return targetRef;
    }

    /**
     * 设置序列流目标节点 ID 引用。
     *
     * @param targetRef 目标节点 ID
     */
    public void setTargetRef(String targetRef) {
        this.targetRef = targetRef;
    }

    /**
     * 获取序列流名称。
     *
     * @return 序列流名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置序列流名称。
     *
     * @param name 序列流名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取序列流条件表达式。
     * 用于网关节点的条件分支判断。
     *
     * @return 条件表达式字符串
     */
    public String getConditionExpression() {
        return conditionExpression;
    }

    /**
     * 设置序列流条件表达式。
     *
     * @param conditionExpression 条件表达式
     */
    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    /**
     * 判断是否为默认流向。
     * 仅在排他网关中使用，当所有条件都不满足时走默认流向。
     *
     * @return 如果是默认流向返回 true
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * 设置是否为默认流向。
     *
     * @param isDefault 是否为默认流向
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 获取序列流的所有属性映射。
     *
     * @return 属性映射表
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * 设置序列流的属性映射。
     *
     * @param properties 属性映射表
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * 设置一个属性值。
     *
     * @param key   属性键
     * @param value 属性值
     */
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }

    /**
     * 获取指定键的属性值。
     *
     * @param key 属性键
     * @return 属性值，如果不存在则返回 null
     */
    public String getProperty(String key) {
        return this.properties.get(key);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "BpmnSequenceFlow{id='" + id + "', sourceRef='" + sourceRef + "', targetRef='" + targetRef +
                "', name='" + name + "', conditionExpression='" + conditionExpression + "', isDefault=" + isDefault + "}";
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
        BpmnSequenceFlow that = (BpmnSequenceFlow) o;
        return isDefault == that.isDefault &&
                Objects.equals(id, that.id) &&
                Objects.equals(sourceRef, that.sourceRef) &&
                Objects.equals(targetRef, that.targetRef) &&
                Objects.equals(name, that.name) &&
                Objects.equals(conditionExpression, that.conditionExpression);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(id, sourceRef, targetRef, name, conditionExpression, isDefault);
    }
}
