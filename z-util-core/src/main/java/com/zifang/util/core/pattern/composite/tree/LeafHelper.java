package com.zifang.util.core.pattern.composite.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 针对叶子结点的操作工具类
 *
 * @author zifang
 */
public class LeafHelper {

    /**
     * 转换平铺的叶子结点得到一个根节点
     */
    public static ILeaf pickRootLeaf(List<? extends ILeaf> leaves) {
        if (leaves == null) {
            throw new NullPointerException();
        }
        return leaves.stream().filter(ILeaf::isRoot).findFirst().orElse(null);
    }

    /**
     * 是否包含游离的根节点（多个根节点）
     */
    public static boolean hasDissociateLeaf(List<? extends ILeaf> leaves) {
        if (leaves == null) {
            throw new NullPointerException();
        }
        return leaves.stream().filter(ILeaf::isRoot).count() > 1;
    }

    /**
     * 获得平铺的叶子结点里面所有的根节点串
     */
    public static List<ILeaf> solveStackLeaves(List<ILeaf> leaves) {
        if (leaves == null) {
            throw new NullPointerException();
        }
        return leaves.stream()
                .filter(ILeaf::isRoot)
                .collect(Collectors.toList());
    }

    /**
     * 对一般化object进行leaf的包装
     */
    public static <ID, C> LeafWrapper<ID, ID, C> wrapper(ID currentId, ID parentId, C bean) {
        return new LeafWrapper<>(currentId, parentId, bean);
    }

    /**
     * 树化结点包装
     */
    public static <ID, C> LeafWrapper<ID, ID, C> treeify(List<LeafWrapper<ID, ID, C>> leafWrappers) {
        Map<ID, LeafWrapper<ID, ID, C>> leafWrapperMap = leafWrappers.stream()
                .collect(Collectors.toMap(LeafWrapper::getA, e -> e));

        LeafWrapper<ID, ID, C> root = null;
        for (LeafWrapper<ID, ID, C> leafWrapper : leafWrappers) {
            LeafWrapper parent = leafWrapperMap.get(leafWrapper.getB());
            if (parent == null) {
                root = leafWrapper;
            } else {
                leafWrapper.setParent(leafWrapper);
                parent.appendSubLeaf(leafWrapper);
            }
        }
        return root;
    }

    /**
     * 收集所有匹配的节点
     */
    static void collectMatches(ILeaf node, Predicate<ILeaf> predicate, List<ILeaf> results) {
        if (predicate.test(node)) {
            results.add(node);
        }
        if (!node.isLeaf()) {
            for (ILeaf child : node.getSubLeaves()) {
                collectMatches(child, predicate, results);
            }
        }
    }

    /**
     * 收集所有到叶子节点的路径
     */
    static void collectLeafPaths(ILeaf node, List<ILeaf> currentPath, List<List<ILeaf>> paths) {
        currentPath.add(node);
        if (node.isLeaf()) {
            paths.add(new ArrayList<>(currentPath));
        } else {
            for (ILeaf child : node.getSubLeaves()) {
                collectLeafPaths(child, currentPath, paths);
            }
        }
        currentPath.remove(currentPath.size() - 1);
    }
}
