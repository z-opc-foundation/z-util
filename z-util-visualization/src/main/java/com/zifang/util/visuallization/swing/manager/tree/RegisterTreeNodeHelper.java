package com.zifang.util.visuallization.swing.manager.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 树形节点注册帮助类
 * 负责将注册的节点转换为Swing可用的树形结构
 */
public class RegisterTreeNodeHelper {
    /**
     * 将注册的节点转换为DefaultMutableTreeNode
     *
     * @param registerTreeNode 节点注册器
     * @return Swing树形结构的根节点
     * @throws RuntimeException 如果有多个根节点或存在游离节点
     */
    public static DefaultMutableTreeNode solve(RegisterTreeNode registerTreeNode) {

        List<TreeNode> roots = registerTreeNode.getTreeNodes().stream().filter(e -> null == e.getParentId()).collect(Collectors.toList());

        if (roots.size() != 1) {
            throw new RuntimeException("有多个根节点或者游离节点");
        }

        TreeNode root = roots.get(0);// 得到根节点
        DefaultMutableTreeNode rootDefaultMutableTreeNode = getDefaultMutableTreeNodeByTreeNode(root); //通过根节点得到节点数据

        solveRoot(root, rootDefaultMutableTreeNode, registerTreeNode.getTreeNodes());
        // 找到根节点

        return rootDefaultMutableTreeNode;
    }

    /**
     * 将TreeNode转换为DefaultMutableTreeNode
     *
     * @param root 树节点
     * @return Swing树形结构的节点
     */
    private static DefaultMutableTreeNode getDefaultMutableTreeNodeByTreeNode(TreeNode root) {
        return new DefaultMutableTreeNode(root.getName());
    }

    /**
     * 递归构建子树结构
     *
     * @param root                       当前根节点
     * @param rootDefaultMutableTreeNode Swing树中的当前节点
     * @param registerTreeNode           所有注册的节点列表
     */
    private static void solveRoot(TreeNode root, DefaultMutableTreeNode rootDefaultMutableTreeNode, List<TreeNode> registerTreeNode) {
        // 得到这个集合的下等级别的数据
        String id = root.getId();
        List<TreeNode> treeNodes = registerTreeNode.stream().filter(e -> id.equals(e.getParentId())).collect(Collectors.toList());
        for (TreeNode treeNode : treeNodes) {
            DefaultMutableTreeNode defaultMutableTreeNode = getDefaultMutableTreeNodeByTreeNode(treeNode);
            rootDefaultMutableTreeNode.add(defaultMutableTreeNode);
            solveRoot(treeNode, defaultMutableTreeNode, registerTreeNode);
        }
    }
}
