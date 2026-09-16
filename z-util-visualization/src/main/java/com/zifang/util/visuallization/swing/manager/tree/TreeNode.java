package com.zifang.util.visuallization.swing.manager.tree;

/**
 * 树形节点数据类
 * 存储树形节点的ID、名称和父节点ID信息
 */
public class TreeNode {
    private String id;
    private String name;
    private String parentId;

    /**
     * 创建树节点
     */
    public TreeNode() {
    }

    /**
     * 创建树节点
     *
     * @param id       节点唯一标识
     * @param name     节点显示名称
     * @param parentId 父节点ID（null表示根节点）
     */
    public TreeNode(String id, String name, String parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    /**
     * 获取节点ID
     *
     * @return 节点ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置节点ID
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
     * 获取父节点ID
     *
     * @return 父节点ID，null表示根节点
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * 设置父节点ID
     *
     * @param parentId 父节点ID
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * 返回树节点的字符串表示
     *
     * @return 包含id、name和parentId的字符串
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "TreeNode{id=" + id + ", name=" + name + ", parentId=" + parentId + "}";
    }

    /**
     * 判断当前树节点与指定对象是否相等
     * 比较基于id、name和parentId
     *
     * @param o 要比较的对象
     * @return 如果所有属性都相同返回true，否则返回false
     */
    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode treeNode = (TreeNode) o;
        return java.util.Objects.equals(id, treeNode.id) &&
                java.util.Objects.equals(name, treeNode.name) &&
                java.util.Objects.equals(parentId, treeNode.parentId);
    }

    /**
     * 返回当前树节点的哈希码
     * 基于id、name和parentId计算
     *
     * @return 哈希码值
     */
    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(id, name, parentId);
    }
}
