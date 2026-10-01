package com.zifang.util.core.pattern.composite.tree;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 组合模式树结构
 *
 * @author zifang
 */
public class Tree {

    private final ILeaf root;

    /**
     * Tree方法。
     * * @param root ILeaf类型参数
     */
    public Tree(ILeaf root) {
        if (root == null) {
            throw new IllegalArgumentException("Root cannot be null");
        }
        if (!root.isRoot()) {
            throw new IllegalArgumentException("Root must be a root node");
        }
        this.root = root;
    }

    /**
     * getRoot方法。
     *
     * @return ILeaf类型返回值
     */
    public ILeaf getRoot() {
        return root;
    }

    // ==================== 遍历操作 ====================

    /**
     * dfs方法。
     * * @param action ConsumerILeaf类型参数
     */
    public void dfs(Consumer<ILeaf> action) {
        Deque<ILeaf> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            ILeaf node = stack.pop();
            action.accept(node);
            if (!node.isLeaf()) {
                List<ILeaf> children = node.getSubLeaves();
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
        }
    }

    /**
     * bfs方法。
     * * @param action ConsumerILeaf类型参数
     */
    public void bfs(Consumer<ILeaf> action) {
        Queue<ILeaf> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            ILeaf node = queue.poll();
            action.accept(node);
            if (!node.isLeaf()) {
                queue.addAll(node.getSubLeaves());
            }
        }
    }

    /**
     * preOrder方法。
     * * @param action ConsumerILeaf类型参数
     */
    public void preOrder(Consumer<ILeaf> action) {
        root.traversePreOrder(action);
    }

    /**
     * postOrder方法。
     * * @param action ConsumerILeaf类型参数
     */
    public void postOrder(Consumer<ILeaf> action) {
        root.traversePostOrder(action);
    }

    /**
     * levelOrder方法。
     * * @param action ConsumerILeaf类型参数
     */
    public void levelOrder(Consumer<ILeaf> action) {
        bfs(action);
    }

    /**
     * levelOrder方法。
     * * @param action BiConsumerInteger,类型参数
     */
    public void levelOrder(BiConsumer<Integer, ILeaf> action) {
        Queue<AbstractMap.SimpleEntry<Integer, ILeaf>> queue = new LinkedList<>();
        queue.offer(new AbstractMap.SimpleEntry<>(0, root));
        while (!queue.isEmpty()) {
            AbstractMap.SimpleEntry<Integer, ILeaf> entry = queue.poll();
            action.accept(entry.getKey(), entry.getValue());
            if (!entry.getValue().isLeaf()) {
                for (ILeaf child : entry.getValue().getSubLeaves()) {
                    queue.offer(new AbstractMap.SimpleEntry<>(entry.getKey() + 1, child));
                }
            }
        }
    }

    /**
     * traverseLeaves方法。
     * * @param action ConsumerILeaf类型参数
     */
    public void traverseLeaves(Consumer<ILeaf> action) {
        dfs(node -> {
            if (node.isLeaf()) {
                action.accept(node);
            }
        });
    }

    // ==================== 搜索操作 ====================

    /**
     * find方法。
     * * @param predicate PredicateILeaf类型参数
     *
     * @return ILeaf类型返回值
     */
    public ILeaf find(Predicate<ILeaf> predicate) {
        return root.find(predicate);
    }

    /**
     * findByName方法。
     * * @param name String类型参数
     *
     * @return ILeaf类型返回值
     */
    public ILeaf findByName(String name) {
        return root.find(node -> Objects.equals(node.getName(), name));
    }

    /**
     * findById方法。
     * * @param id String类型参数
     *
     * @return ILeaf类型返回值
     */
    public ILeaf findById(String id) {
        return root.find(node -> Objects.equals(node.getId(), id));
    }

    /**
     * findAll方法。
     * * @param predicate PredicateILeaf类型参数
     *
     * @return List<ILeaf>类型返回值
     */
    public List<ILeaf> findAll(Predicate<ILeaf> predicate) {
        return root.findAll(predicate);
    }

    /**
     * findAllByName方法。
     * * @param name String类型参数
     *
     * @return List<ILeaf>类型返回值
     */
    public List<ILeaf> findAllByName(String name) {
        return root.findAll(node -> Objects.equals(node.getName(), name));
    }

    /**
     * findAllPaths方法。
     * * @param predicate PredicateILeaf类型参数
     *
     * @return List<List<ILeaf>>类型返回值
     */
    public List<List<ILeaf>> findAllPaths(Predicate<ILeaf> predicate) {
        List<List<ILeaf>> paths = new ArrayList<>();
        findPathsRecursive(root, new ArrayList<>(), predicate, paths);
        return paths;
    }

    private void findPathsRecursive(ILeaf node, List<ILeaf> currentPath, Predicate<ILeaf> predicate, List<List<ILeaf>> paths) {
        currentPath.add(node);
        if (predicate.test(node)) {
            paths.add(new ArrayList<>(currentPath));
        }
        if (!node.isLeaf()) {
            for (ILeaf child : node.getSubLeaves()) {
                findPathsRecursive(child, currentPath, predicate, paths);
            }
        }
        currentPath.remove(currentPath.size() - 1);
    }

    // ==================== 路径操作 ====================

    /**
     * getPath方法。
     * * @param target ILeaf类型参数
     *
     * @return List<ILeaf>类型返回值
     */
    public List<ILeaf> getPath(ILeaf target) {
        return target.getPathFromRoot();
    }

    /**
     * getLowestCommonAncestor方法。
     * * @param node1 ILeaf类型参数
     *
     * @param node2 ILeaf类型参数
     * @return ILeaf类型返回值
     */
    public ILeaf getLowestCommonAncestor(ILeaf node1, ILeaf node2) {
        Set<ILeaf> ancestors1 = new HashSet<>();
        ILeaf current = node1;
        while (current != null) {
            ancestors1.add(current);
            current = current.getParentLeaf();
        }
        current = node2;
        while (current != null) {
            if (ancestors1.contains(current)) {
                return current;
            }
            current = current.getParentLeaf();
        }
        return null;
    }

    /**
     * getDistance方法。
     * * @param node1 ILeaf类型参数
     *
     * @param node2 ILeaf类型参数
     * @return int类型返回值
     */
    public int getDistance(ILeaf node1, ILeaf node2) {
        ILeaf lca = getLowestCommonAncestor(node1, node2);
        if (lca == null) {
            return -1;
        }
        return node1.getDepth() + node2.getDepth() - 2 * lca.getDepth();
    }

    // ==================== 统计操作 ====================

    /**
     * getNodeCount方法。
     *
     * @return int类型返回值
     */
    public int getNodeCount() {
        return root.getSubtreeSize();
    }

    /**
     * getLeafCount方法。
     *
     * @return int类型返回值
     */
    public int getLeafCount() {
        int[] count = {0};
        traverseLeaves(node -> count[0]++);
        return count[0];
    }

    /**
     * getHeight方法。
     *
     * @return int类型返回值
     */
    public int getHeight() {
        return root.getSubtreeHeight();
    }

    /**
     * getDepth方法。
     *
     * @return int类型返回值
     */
    public int getDepth() {
        return root.getDepth();
    }

    /**
     * getDepth方法。
     * * @param node ILeaf类型参数
     *
     * @return int类型返回值
     */
    public int getDepth(ILeaf node) {
        return node.getDepth();
    }

    /**
     * getDegree方法。
     * * @param node ILeaf类型参数
     *
     * @return int类型返回值
     */
    public int getDegree(ILeaf node) {
        return node.getChildCount();
    }

    /**
     * getAverageDepth方法。
     *
     * @return double类型返回值
     */
    public double getAverageDepth() {
        int[] sum = {0};
        int[] count = {0};
        preOrder(node -> {
            sum[0] += node.getDepth();
            count[0]++;
        });
        return count[0] > 0 ? (double) sum[0] / count[0] : 0;
    }

    // ==================== 验证操作 ====================

    /**
     * isBinaryTree方法。
     *
     * @return boolean类型返回值
     */
    public boolean isBinaryTree() {
        int[] result = {1};
        checkBinaryTree(root, result);
        return result[0] == 1;
    }

    private void checkBinaryTree(ILeaf node, int[] result) {
        if (node.isLeaf()) {
            return;
        }
        if (node.getChildCount() > 2) {
            result[0] = 0;
            return;
        }
        if (node.getChildCount() >= 1) {
            checkBinaryTree(node.getSubLeaves().get(0), result);
        }
        if (result[0] == 0) return;
        if (node.getChildCount() >= 2) {
            checkBinaryTree(node.getSubLeaves().get(1), result);
        }
    }

    /**
     * isBalanced方法。
     *
     * @return boolean类型返回值
     */
    public boolean isBalanced() {
        return checkBalanced(root) >= 0;
    }

    private int checkBalanced(ILeaf node) {
        if (node == null || node.isLeaf()) {
            return 0;
        }
        int leftHeight = 0;
        int rightHeight = 0;

        if (node.getChildCount() >= 1) {
            leftHeight = checkBalanced(node.getSubLeaves().get(0));
        }
        if (leftHeight < 0) return -1;

        if (node.getChildCount() >= 2) {
            rightHeight = checkBalanced(node.getSubLeaves().get(1));
        }
        if (rightHeight < 0) return -1;

        if (Math.abs(leftHeight - rightHeight) > 1) {
            return -1;
        }
        return 1 + Math.max(leftHeight, rightHeight);
    }

    // ==================== 转换操作 ====================

    /**
     * toList方法。
     *
     * @return List<ILeaf>类型返回值
     */
    public List<ILeaf> toList() {
        List<ILeaf> result = new ArrayList<>();
        preOrder(result::add);
        return result;
    }

    /**
     * toMap方法。
     *
     * @return Map<String, ILeaf>类型返回值
     */
    public Map<String, ILeaf> toMap() {
        Map<String, ILeaf> result = new LinkedHashMap<>();
        preOrder(node -> result.put(node.getId(), node));
        return result;
    }

    /**
     * map方法。
     * * @param mapper FunctionILeaf,类型参数
     *
     * @return <R> List<R>类型返回值
     */
    public <R> List<R> map(Function<ILeaf, R> mapper) {
        return root.map(mapper);
    }

    // ==================== 打印操作 ====================

    /**
     * print方法。
     */
    public void print() {
        System.out.println(toTreeString());
    }

    /**
     * toTreeString方法。
     *
     * @return String类型返回值
     */
    public String toTreeString() {
        return root.toTreeString();
    }

    /**
     * toIndentString方法。
     *
     * @return String类型返回值
     */
    public String toIndentString() {
        StringBuilder sb = new StringBuilder();
        printIndent(root, 0, sb);
        return sb.toString();
    }

    private void printIndent(ILeaf node, int depth, StringBuilder sb) {
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(node.getName()).append("\n");
        if (!node.isLeaf()) {
            for (ILeaf child : node.getSubLeaves()) {
                printIndent(child, depth + 1, sb);
            }
        }
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Tree{root=" + root.getName() + ", size=" + getNodeCount() + ", height=" + getHeight() + "}";
    }
}
