package com.zifang.util.core.pattern.composite.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 组合模式叶子节点接口
 * <p>
 * 同时承担Composite和Leaf的角色：
 * - isLeaf() = getSubLeaves().isEmpty()
 * - 可以通过appendSubLeaf/removeSubLeaf管理子节点
 *
 * @author zifang
 */
public interface ILeaf {

    String ROOT = "ROOT";

    // ==================== 基本操作 ====================

    /**
     * 获得下属的叶子结点
     */
    List<ILeaf> getSubLeaves();

    /**
     * 得到父亲的叶子结点
     */
    ILeaf getParentLeaf();

    /**
     * 判定是否为根结点
     */
    default boolean isRoot() {
        return getParentLeaf() == null;
    }

    /**
     * 是否为叶子节点（无子节点）
     */
    default boolean isLeaf() {
        List<ILeaf> subs = getSubLeaves();
        return subs == null || subs.isEmpty();
    }

    /**
     * 得到当前的结点名字
     */
    default String getName() {
        return this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    /**
     * 得到当前的唯一标识号
     */
    default String getId() {
        return this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    /**
     * 描述当前这个树的情况
     */
    default String describe() {
        return this.toString();
    }

    /**
     * 往叶子结点增加子节点
     */
    void appendSubLeaf(ILeaf leaf);

    /**
     * 叶子结点有能力接受父节点
     */
    void setParent(ILeaf leaf);

    /**
     * 移除子节点
     */
    default void removeSubLeaf(ILeaf leaf) {
        if (leaf != null && getSubLeaves() != null) {
            getSubLeaves().remove(leaf);
            leaf.setParent(null);
        }
    }

    /**
     * 获取子节点数量
     */
    default int getChildCount() {
        List<ILeaf> subs = getSubLeaves();
        return subs == null ? 0 : subs.size();
    }

    /**
     * 获取以该节点为根的子树高度（叶子节点高度为1）
     */
    default int getSubtreeHeight() {
        if (isLeaf()) {
            return 1;
        }
        return 1 + getSubLeaves().stream()
                .mapToInt(ILeaf::getSubtreeHeight)
                .max()
                .orElse(1);
    }

    /**
     * 获取以该节点为根的子树节点总数
     */
    default int getSubtreeSize() {
        if (isLeaf()) {
            return 1;
        }
        return 1 + getSubLeaves().stream()
                .mapToInt(ILeaf::getSubtreeSize)
                .sum();
    }

    /**
     * 获取从根到该节点的路径
     */
    default List<ILeaf> getPathFromRoot() {
        List<ILeaf> path = new ArrayList<>();
        ILeaf current = this;
        while (current != null) {
            path.add(0, current);
            current = current.getParentLeaf();
        }
        return path;
    }

    /**
     * 获取深度（根节点深度为1）
     */
    default int getDepth() {
        return getPathFromRoot().size();
    }

    /**
     * 获取从该节点到所有叶子节点的路径
     */
    default List<List<ILeaf>> getAllPathsToLeaves() {
        List<List<ILeaf>> paths = new ArrayList<>();
        List<ILeaf> currentPath = new ArrayList<>();
        LeafHelper.collectLeafPaths(this, currentPath, paths);
        return paths;
    }

    /**
     * 获取从该节点到第一个叶子节点的路径
     */
    default List<ILeaf> getPathToFirstLeaf() {
        List<List<ILeaf>> allPaths = getAllPathsToLeaves();
        return allPaths.isEmpty() ? new ArrayList<>() : allPaths.get(0);
    }

    // ==================== 遍历操作 ====================

    /**
     * 前序遍历（根-左-右）
     */
    default void traversePreOrder(Consumer<ILeaf> action) {
        action.accept(this);
        if (!isLeaf()) {
            for (ILeaf child : getSubLeaves()) {
                child.traversePreOrder(action);
            }
        }
    }

    /**
     * 后序遍历（左-右-根）
     */
    default void traversePostOrder(Consumer<ILeaf> action) {
        if (!isLeaf()) {
            for (ILeaf child : getSubLeaves()) {
                child.traversePostOrder(action);
            }
        }
        action.accept(this);
    }

    // ==================== 搜索操作 ====================

    /**
     * 查找第一个匹配的节点
     */
    default ILeaf find(Predicate<ILeaf> predicate) {
        if (predicate.test(this)) {
            return this;
        }
        if (!isLeaf()) {
            for (ILeaf child : getSubLeaves()) {
                ILeaf found = child.find(predicate);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * 查找所有匹配的节点
     */
    default List<ILeaf> findAll(Predicate<ILeaf> predicate) {
        List<ILeaf> results = new ArrayList<>();
        LeafHelper.collectMatches(this, predicate, results);
        return results;
    }

    /**
     * 过滤子树
     */
    default List<ILeaf> filter(Predicate<ILeaf> predicate) {
        return findAll(predicate);
    }

    /**
     * 映射节点
     */
    default <R> List<R> map(Function<ILeaf, R> mapper) {
        List<R> results = new ArrayList<>();
        traversePreOrder(node -> results.add(mapper.apply(node)));
        return results;
    }

    /**
     * 是否存在匹配节点
     */
    default boolean exists(Predicate<ILeaf> predicate) {
        return find(predicate) != null;
    }

    /**
     * 获取根节点
     */
    default ILeaf getRoot() {
        ILeaf current = this;
        while (current.getParentLeaf() != null) {
            current = current.getParentLeaf();
        }
        return current;
    }

    /**
     * 打印树形结构
     */
    default String toTreeString() {
        StringBuilder sb = new StringBuilder();
        printTree(0, "", sb);
        return sb.toString();
    }

    default void printTree(int depth, String prefix, StringBuilder sb) {
        sb.append(prefix).append(getName()).append("\n");
        if (!isLeaf()) {
            List<ILeaf> children = getSubLeaves();
            for (int i = 0; i < children.size(); i++) {
                boolean isLast = i == children.size() - 1;
                String newPrefix = prefix + (isLast ? "└── " : "├── ");
                children.get(i).printTree(depth + 1, newPrefix, sb);
            }
        }
    }
}
