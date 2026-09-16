package com.zifang.util.core.pattern.composite.tree;

import java.util.*;

/**
 * 树构建器
 *
 * @author zifang
 */
public class TreeBuilder {

    private final ILeaf root;
    private final Map<String, ILeaf> nodeMap;
    private ILeaf current;

    private TreeBuilder(String name) {
        this.root = new LeafWrapper<>(name, name, name);
        this.nodeMap = new HashMap<>();
        this.nodeMap.put(name, this.root);
        this.current = this.root;
    }

    private TreeBuilder(ILeaf root, Map<String, ILeaf> nodeMap, ILeaf current) {
        this.root = root;
        this.nodeMap = nodeMap;
        this.current = current;
    }

    /**
     * create方法。
     * * @param rootName String类型参数
     *
     * @return static TreeBuilder类型返回值
     */
    public static TreeBuilder create(String rootName) {
        return new TreeBuilder(rootName);
    }

    /**
     * create方法。
     * * @param root ILeaf类型参数
     *
     * @return static TreeBuilder类型返回值
     */
    public static TreeBuilder create(ILeaf root) {
        TreeBuilder builder = new TreeBuilder(root.getName());
        builder.nodeMap.clear();
        builder.nodeMap.put(root.getId(), root);
        return builder;
    }

    /**
     * buildFromList方法。
     * * @param nodes List?类型参数
     *
     * @return static Tree类型返回值
     */
    public static Tree buildFromList(List<? extends ILeaf> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("Nodes cannot be empty");
        }

        Map<String, ILeaf> nodeMap = new HashMap<>();
        for (ILeaf node : nodes) {
            nodeMap.put(node.getId(), node);
        }

        ILeaf root = null;
        for (ILeaf node : nodes) {
            if (node.isRoot()) {
                root = node;
                break;
            }
        }

        if (root == null) {
            throw new IllegalArgumentException("No root node found");
        }

        return new Tree(root);
    }

    /**
     * buildFromHierarchy方法。
     * * @param items List?类型参数
     *
     * @return static Tree类型返回值
     */
    public static Tree buildFromHierarchy(List<? extends HierarchyItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }

        List<ILeaf> nodes = new ArrayList<>();
        for (HierarchyItem item : items) {
            ILeaf node = new LeafWrapper<>(item.getId(), item.getParentId(), item);
            nodes.add(node);
        }

        Map<String, ILeaf> nodeMap = new HashMap<>();
        for (ILeaf node : nodes) {
            nodeMap.put(node.getId(), node);
        }

        ILeaf root = null;
        for (ILeaf node : nodes) {
            @SuppressWarnings("unchecked")
            LeafWrapper<String, String, HierarchyItem> wrapper = (LeafWrapper<String, String, HierarchyItem>) node;
            String parentId = wrapper.getParentId();
            if (parentId == null || parentId.isEmpty() || !nodeMap.containsKey(parentId)) {
                root = node;
            } else {
                ILeaf parent = nodeMap.get(parentId);
                parent.appendSubLeaf(node);
            }
        }

        if (root == null) {
            throw new IllegalArgumentException("No root node found");
        }

        return new Tree(root);
    }

    /**
     * add方法。
     * * @param name String类型参数
     *
     * @return TreeBuilder类型返回值
     */
    public TreeBuilder add(String name) {
        ILeaf child = new LeafWrapper<>(name, name, name);
        current.appendSubLeaf(child);
        nodeMap.put(name, child);
        return this;
    }

    /**
     * add方法。
     * * @param leaf ILeaf类型参数
     *
     * @return TreeBuilder类型返回值
     */
    public TreeBuilder add(ILeaf leaf) {
        current.appendSubLeaf(leaf);
        nodeMap.put(leaf.getId(), leaf);
        return this;
    }

    /**
     * child方法。
     * * @param name String类型参数
     *
     * @return TreeBuilder类型返回值
     */
    public TreeBuilder child(String name) {
        ILeaf child = new LeafWrapper<>(name, name, name);
        current.appendSubLeaf(child);
        nodeMap.put(name, child);
        current = child;
        return this;
    }

    /**
     * up方法。
     *
     * @return TreeBuilder类型返回值
     */
    public TreeBuilder up() {
        if (current.getParentLeaf() != null) {
            current = current.getParentLeaf();
        }
        return this;
    }

    /**
     * down方法。
     * * @param name String类型参数
     *
     * @return TreeBuilder类型返回值
     */
    public TreeBuilder down(String name) {
        ILeaf child = findChildByName(name);
        if (child == null) {
            throw new IllegalArgumentException("Child not found: " + name);
        }
        current = child;
        return this;
    }

    private ILeaf findChildByName(String name) {
        for (ILeaf child : current.getSubLeaves()) {
            if (Objects.equals(child.getName(), name)) {
                return child;
            }
        }
        return null;
    }

    /**
     * name方法。
     * * @param name String类型参数
     *
     * @return TreeBuilder类型返回值
     */
    public TreeBuilder name(String name) {
        if (current instanceof LeafWrapper) {
            ((LeafWrapper<?, ?, ?>) current).setName(name);
        }
        return this;
    }

    /**
     * meta方法。
     * * @param key String类型参数
     *
     * @param value Object类型参数
     * @return TreeBuilder类型返回值
     */
    public TreeBuilder meta(String key, Object value) {
        if (current instanceof LeafWrapper) {
            ((LeafWrapper<?, ?, ?>) current).setMeta(key, value);
        }
        return this;
    }

    /**
     * current方法。
     *
     * @return ILeaf类型返回值
     */
    public ILeaf current() {
        return current;
    }

    /**
     * root方法。
     *
     * @return ILeaf类型返回值
     */
    public ILeaf root() {
        return root;
    }

    /**
     * build方法。
     *
     * @return Tree类型返回值
     */
    public Tree build() {
        return new Tree(root);
    }

    /**
     * 层级数据项接口
     */
    public interface HierarchyItem {
        String getId();

        String getParentId();
    }
}
