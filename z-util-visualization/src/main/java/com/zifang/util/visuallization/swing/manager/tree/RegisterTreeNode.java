package com.zifang.util.visuallization.swing.manager.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 树形节点注册器
 * 用于注册和管理树形结构中的节点信息
 */
public class RegisterTreeNode {

    private List<TreeNode> treeNodes = new ArrayList<>();

    /**
     * 创建树节点注册器
     */
    public RegisterTreeNode() {
    }

    /**
     * 获取所有注册的节点
     *
     * @return 节点列表
     */
    public List<TreeNode> getTreeNodes() {
        return treeNodes;
    }

    /**
     * 设置节点列表
     *
     * @param treeNodes 节点列表
     */
    public void setTreeNodes(List<TreeNode> treeNodes) {
        this.treeNodes = treeNodes;
    }

    /**
     * 注册一个节点（支持链式调用）
     *
     * @param treeNode 要注册的节点
     * @return 当前注册器实例
     */
    public RegisterTreeNode register(TreeNode treeNode) {
        treeNodes.add(treeNode);
        return this;
    }

    /**
     * 返回树节点注册器的字符串表示
     *
     * @return 包含所有树节点的字符串
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "RegisterTreeNode{treeNodes=" + treeNodes + "}";
    }

    /**
     * 判断当前注册器与指定对象是否相等
     * 比较基于树节点列表
     *
     * @param o 要比较的对象
     * @return 如果树节点列表相同返回true，否则返回false
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
        RegisterTreeNode that = (RegisterTreeNode) o;
        return Objects.equals(treeNodes, that.treeNodes);
    }

    /**
     * 返回当前注册器的哈希码
     * 基于树节点列表计算
     *
     * @return 哈希码值
     */
    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(treeNodes);
    }
}
