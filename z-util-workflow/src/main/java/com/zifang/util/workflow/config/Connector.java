package com.zifang.util.workflow.config;

import java.util.List;

/**
 * 节点连接器。
 * <p>
 * 描述此节点与其他节点的关联情况，包含前置节点列表和后置节点列表。
 * 用于构建工作流的DAG（有向无环图）结构。
 *
 * @see WorkflowNode
 */
public class Connector {

    /**
     * 前置节点列表
     */
    private List<String> pre;

    /**
     * 后置节点列表
     */
    private List<String> post;

    /**
     * 默认构造函数
     */
    public Connector() {
    }

    /**
     * 全参数构造函数
     *
     * @param pre  前置节点ID列表
     * @param post 后置节点ID列表
     */
    public Connector(List<String> pre, List<String> post) {
        this.pre = pre;
        this.post = post;
    }

    /**
     * 获取前置节点ID列表
     *
     * @return 前置节点ID列表
     */
    public List<String> getPre() {
        return pre;
    }

    /**
     * 设置前置节点ID列表
     *
     * @param pre 前置节点ID列表
     */
    public void setPre(List<String> pre) {
        this.pre = pre;
    }

    /**
     * 获取后置节点ID列表
     *
     * @return 后置节点ID列表
     */
    public List<String> getPost() {
        return post;
    }

    /**
     * 设置后置节点ID列表
     *
     * @param post 后置节点ID列表
     */
    public void setPost(List<String> post) {
        this.post = post;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Connector{pre=" + pre + ", post=" + post + "}";
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
        Connector connector = (Connector) o;
        if (pre != null ? !pre.equals(connector.pre) : connector.pre != null) return false;
        return post != null ? post.equals(connector.post) : connector.post == null;
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        int result = pre != null ? pre.hashCode() : 0;
        result = 31 * result + (post != null ? post.hashCode() : 0);
        return result;
    }
}
