package com.zifang.util.core.pattern.composite;

import com.zifang.util.core.pattern.composite.tree.*;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * CompositeTest类。
 */
public class CompositeTest {

    // ==================== ILeaf Tests ====================

    @Test
    /**
     * testILeafBasicOperations方法。
     */
    public void testILeafBasicOperations() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> leaf = new LeafWrapper<>("leaf", "child1", "leaf");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(leaf);

        assertEquals("root", root.getName());
        assertEquals(2, root.getChildCount());
        assertEquals(2, root.getSubLeaves().size());
        assertTrue(root.isRoot());
        assertFalse(root.isLeaf());

        assertEquals(root, child1.getParentLeaf());
        assertTrue(leaf.isLeaf());
    }

    @Test
    /**
     * testGetDepth方法。
     */
    public void testGetDepth() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(grandchild);

        // root depth = 1, children depth = 2, grandchild depth = 3
        assertEquals(1, root.getDepth());
        assertEquals(2, child1.getDepth());
        assertEquals(2, child2.getDepth());
        assertEquals(3, grandchild.getDepth());
    }

    @Test
    /**
     * testGetSubtreeSize方法。
     */
    public void testGetSubtreeSize() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(grandchild);

        assertEquals(4, root.getSubtreeSize());
        assertEquals(2, child1.getSubtreeSize());
        assertEquals(1, child2.getSubtreeSize());
        assertEquals(1, grandchild.getSubtreeSize());
    }

    @Test
    /**
     * testGetPathFromRoot方法。
     */
    public void testGetPathFromRoot() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        child1.appendSubLeaf(grandchild);

        List<ILeaf> path = grandchild.getPathFromRoot();
        assertEquals(3, path.size());
        assertEquals("root", path.get(0).getName());
        assertEquals("child1", path.get(1).getName());
        assertEquals("grandchild", path.get(2).getName());
    }

    @Test
    /**
     * testTraversePreOrder方法。
     */
    public void testTraversePreOrder() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        StringBuilder result = new StringBuilder();
        root.traversePreOrder(node -> result.append(node.getName()).append("-"));

        assertEquals("root-child1-child2-", result.toString());
    }

    @Test
    /**
     * testTraversePostOrder方法。
     */
    public void testTraversePostOrder() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        StringBuilder result = new StringBuilder();
        root.traversePostOrder(node -> result.append(node.getName()).append("-"));

        assertEquals("child1-child2-root-", result.toString());
    }

    @Test
    /**
     * testFind方法。
     */
    public void testFind() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        assertEquals(child2, root.find(node -> node.getName().equals("child2")));
        assertNull(root.find(node -> node.getName().equals("nonexistent")));
    }

    @Test
    /**
     * testFindAll方法。
     */
    public void testFindAll() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child", "root", "child");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child", "root", "child");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        List<ILeaf> found = root.findAll(node -> node.getName().equals("child"));
        assertEquals(2, found.size());
    }

    @Test
    /**
     * testMap方法。
     */
    public void testMap() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        List<String> names = root.map(ILeaf::getName);
        assertEquals(3, names.size());
        assertTrue(names.contains("root"));
        assertTrue(names.contains("child1"));
        assertTrue(names.contains("child2"));
    }

    @Test
    /**
     * testGetRoot方法。
     */
    public void testGetRoot() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        child1.appendSubLeaf(grandchild);

        assertEquals(root, grandchild.getRoot());
        assertEquals(root, child1.getRoot());
        assertEquals(root, root.getRoot());
    }

    // ==================== LeafWrapper Tests ====================

    @Test
    /**
     * testLeafWrapperMetadata方法。
     */
    public void testLeafWrapperMetadata() {
        LeafWrapper<String, String, String> node = new LeafWrapper<>("node", null, "node");
        node.setMeta("key1", "value1");
        node.setMeta("key2", "value2");

        assertEquals("value1", node.getMeta("key1"));
        assertEquals("value2", node.getMeta("key2"));
        assertEquals(2, node.getMetadata().size());
    }

    @Test
    /**
     * testLeafWrapperRemoveSubLeaf方法。
     */
    public void testLeafWrapperRemoveSubLeaf() {
        LeafWrapper<String, String, String> parent = new LeafWrapper<>("parent", null, "parent");
        LeafWrapper<String, String, String> child = new LeafWrapper<>("child", "parent", "child");

        parent.appendSubLeaf(child);
        assertEquals(1, parent.getChildCount());

        parent.removeSubLeaf(child);
        assertEquals(0, parent.getChildCount());
        assertTrue(child.isRoot());
    }

    @Test
    /**
     * testLeafWrapperSortChildren方法。
     */
    public void testLeafWrapperSortChildren() {
        LeafWrapper<String, String, String> parent = new LeafWrapper<>("parent", null, "parent");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("b", "parent", "b");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("a", "parent", "a");
        LeafWrapper<String, String, String> child3 = new LeafWrapper<>("c", "parent", "c");

        parent.appendSubLeaf(child1);
        parent.appendSubLeaf(child2);
        parent.appendSubLeaf(child3);

        parent.getSubLeaves().sort(Comparator.comparing(ILeaf::getName));

        assertEquals("a", parent.getSubLeaves().get(0).getName());
        assertEquals("b", parent.getSubLeaves().get(1).getName());
        assertEquals("c", parent.getSubLeaves().get(2).getName());
    }

    @Test
    /**
     * testGetAllLeaves方法。
     */
    public void testGetAllLeaves() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> leaf1 = new LeafWrapper<>("leaf1", "child1", "leaf1");
        LeafWrapper<String, String, String> leaf2 = new LeafWrapper<>("leaf2", "child2", "leaf2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(leaf1);
        child2.appendSubLeaf(leaf2);

        List<ILeaf> leaves = root.findAll(ILeaf::isLeaf);
        assertEquals(2, leaves.size());
    }

    // ==================== Tree Tests ====================

    @Test
    /**
     * testTreeCreation方法。
     */
    public void testTreeCreation() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        Tree tree = new Tree(root);
        assertEquals(root, tree.getRoot());
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testTreeCreationWithNullRoot方法。
     */
    public void testTreeCreationWithNullRoot() {
        new Tree(null);
    }

    @Test
    /**
     * testTreeDfs方法。
     */
    public void testTreeDfs() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        Tree tree = new Tree(root);
        StringBuilder result = new StringBuilder();
        tree.dfs(node -> result.append(node.getName()).append("-"));

        assertEquals("root-child1-child2-", result.toString());
    }

    @Test
    /**
     * testTreeBfs方法。
     */
    public void testTreeBfs() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        Tree tree = new Tree(root);
        StringBuilder result = new StringBuilder();
        tree.bfs(node -> result.append(node.getName()).append("-"));

        assertEquals("root-child1-child2-", result.toString());
    }

    @Test
    /**
     * testTreeFind方法。
     */
    public void testTreeFind() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        Tree tree = new Tree(root);
        assertEquals(child2, tree.find(node -> node.getName().equals("child2")));
        assertNull(tree.find(node -> node.getName().equals("nonexistent")));
    }

    @Test
    /**
     * testTreeFindAll方法。
     */
    public void testTreeFindAll() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child", "root", "child");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child", "root", "child");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        Tree tree = new Tree(root);
        List<ILeaf> found = tree.findAll(node -> node.getName().equals("child"));
        assertEquals(2, found.size());
    }

    @Test
    /**
     * testTreeGetNodeCount方法。
     */
    public void testTreeGetNodeCount() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(grandchild);

        Tree tree = new Tree(root);
        assertEquals(4, tree.getNodeCount());
    }

    @Test
    /**
     * testTreeGetHeight方法。
     */
    public void testTreeGetHeight() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        child1.appendSubLeaf(grandchild);

        Tree tree = new Tree(root);
        assertEquals(3, tree.getHeight());
    }

    @Test
    /**
     * testTreeGetLeafCount方法。
     */
    public void testTreeGetLeafCount() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        Tree tree = new Tree(root);
        assertEquals(2, tree.getLeafCount());
    }

    @Test
    /**
     * testTreeGetLCA方法。
     */
    public void testTreeGetLCA() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(grandchild);

        Tree tree = new Tree(root);
        assertEquals(root, tree.getLowestCommonAncestor(grandchild, child2));
        assertEquals(child1, tree.getLowestCommonAncestor(grandchild, child1));
        assertEquals(root, tree.getLowestCommonAncestor(child1, child2));
    }

    @Test
    /**
     * testTreeGetDistance方法。
     */
    public void testTreeGetDistance() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(grandchild);

        Tree tree = new Tree(root);
        assertEquals(3, tree.getDistance(grandchild, child2));
        assertEquals(0, tree.getDistance(child1, child1));
        assertEquals(1, tree.getDistance(root, child1));
    }

    @Test
    /**
     * testTreeToList方法。
     */
    public void testTreeToList() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        Tree tree = new Tree(root);
        List<ILeaf> list = tree.toList();
        assertEquals(3, list.size());
    }

    @Test
    /**
     * testTreeToMap方法。
     */
    public void testTreeToMap() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");

        root.appendSubLeaf(child1);

        Tree tree = new Tree(root);
        Map<String, ILeaf> map = tree.toMap();
        assertEquals(2, map.size());
    }

    @Test
    /**
     * testTreeGetAverageDepth方法。
     */
    public void testTreeGetAverageDepth() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> grandchild = new LeafWrapper<>("grandchild", "child1", "grandchild");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(grandchild);

        Tree tree = new Tree(root);
        double avgDepth = tree.getAverageDepth();
        assertTrue(avgDepth > 0 && avgDepth <= 3);
    }

    @Test
    /**
     * testTreeFindAllPaths方法。
     */
    public void testTreeFindAllPaths() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");
        LeafWrapper<String, String, String> grandchild1 = new LeafWrapper<>("leaf", "child1", "leaf");
        LeafWrapper<String, String, String> grandchild2 = new LeafWrapper<>("leaf", "child2", "leaf");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);
        child1.appendSubLeaf(grandchild1);
        child2.appendSubLeaf(grandchild2);

        Tree tree = new Tree(root);
        List<List<ILeaf>> paths = tree.findAllPaths(node -> node.getName().equals("leaf"));
        assertEquals(2, paths.size());
        assertEquals(3, paths.get(0).size());
    }

    @Test
    /**
     * testTreeLevelOrderWithDepth方法。
     */
    public void testTreeLevelOrderWithDepth() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> child1 = new LeafWrapper<>("child1", "root", "child1");
        LeafWrapper<String, String, String> child2 = new LeafWrapper<>("child2", "root", "child2");

        root.appendSubLeaf(child1);
        root.appendSubLeaf(child2);

        Tree tree = new Tree(root);
        StringBuilder result = new StringBuilder();
        tree.levelOrder((depth, node) -> result.append(depth).append(":").append(node.getName()).append("-"));

        assertTrue(result.toString().contains("0:root"));
        assertTrue(result.toString().contains("1:child1"));
        assertTrue(result.toString().contains("1:child2"));
    }

    @Test
    /**
     * testIsBinaryTree方法。
     */
    public void testIsBinaryTree() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> left = new LeafWrapper<>("left", "root", "left");
        LeafWrapper<String, String, String> right = new LeafWrapper<>("right", "root", "right");

        root.appendSubLeaf(left);
        root.appendSubLeaf(right);

        Tree tree = new Tree(root);
        assertTrue(tree.isBinaryTree());
    }

    @Test
    /**
     * testIsBalanced方法。
     */
    public void testIsBalanced() {
        LeafWrapper<String, String, String> root = new LeafWrapper<>("root", null, "root");
        LeafWrapper<String, String, String> left = new LeafWrapper<>("left", "root", "left");
        LeafWrapper<String, String, String> right = new LeafWrapper<>("right", "root", "right");

        root.appendSubLeaf(left);
        root.appendSubLeaf(right);

        Tree tree = new Tree(root);
        assertTrue(tree.isBalanced());
    }

    // ==================== TreeBuilder Tests ====================

    @Test
    /**
     * testTreeBuilderBasic方法。
     */
    public void testTreeBuilderBasic() {
        TreeBuilder builder = TreeBuilder.create("root");
        builder.add("child1");
        builder.add("child2");
        builder.add("child3");

        Tree tree = builder.build();
        assertEquals("root", tree.getRoot().getName());
        assertEquals(3, tree.getRoot().getChildCount());
    }

    @Test
    /**
     * testTreeBuilderWithDown方法。
     */
    public void testTreeBuilderWithDown() {
        TreeBuilder builder = TreeBuilder.create("root");
        builder.add("child1");
        builder.down("child1").add("grandchild");
        builder.up().add("child2");

        Tree tree = builder.build();
        assertEquals("root", tree.getRoot().getName());
        assertEquals(2, tree.getRoot().getChildCount());
        assertEquals(1, tree.getRoot().getSubLeaves().get(0).getChildCount());
    }

    @Test
    /**
     * testTreeBuilderMetadata方法。
     */
    public void testTreeBuilderMetadata() {
        TreeBuilder builder = TreeBuilder.create("root");
        builder.add("child1");
        builder.down("child1").meta("key", "value");

        Tree tree = builder.build();
        ILeaf child = tree.getRoot().getSubLeaves().get(0);
        assertEquals("value", ((LeafWrapper<?, ?, ?>) child).getMeta("key"));
    }

    @Test
    /**
     * testTreeBuilderBuildFromHierarchy方法。
     */
    public void testTreeBuilderBuildFromHierarchy() {
        List<TreeBuilder.HierarchyItem> items = new java.util.ArrayList<>();
        items.add(new TreeBuilder.HierarchyItem() {
            @Override
            /**
             * getId方法。
             * @return String类型返回值
             */
            public String getId() {
                return "1";
            }

            @Override
            /**
             * getParentId方法。
             * @return String类型返回值
             */
            public String getParentId() {
                return null;
            }
        });
        items.add(new TreeBuilder.HierarchyItem() {
            @Override
            /**
             * getId方法。
             * @return String类型返回值
             */
            public String getId() {
                return "2";
            }

            @Override
            /**
             * getParentId方法。
             * @return String类型返回值
             */
            public String getParentId() {
                return "1";
            }
        });
        items.add(new TreeBuilder.HierarchyItem() {
            @Override
            /**
             * getId方法。
             * @return String类型返回值
             */
            public String getId() {
                return "3";
            }

            @Override
            /**
             * getParentId方法。
             * @return String类型返回值
             */
            public String getParentId() {
                return "1";
            }
        });

        Tree tree = TreeBuilder.buildFromHierarchy(items);

        assertEquals("1", tree.getRoot().getId());
        assertEquals(2, tree.getRoot().getChildCount());
    }

    // ==================== LeafHelper Tests ====================

    @Test
    /**
     * testLeafHelperPickRoot方法。
     */
    public void testLeafHelperPickRoot() {
        LeafWrapper<String, String, String> root1 = new LeafWrapper<>("root1", null, "root1");
        LeafWrapper<String, String, String> root2 = new LeafWrapper<>("root2", null, "root2");

        assertEquals(root1, LeafHelper.pickRootLeaf(java.util.Arrays.asList(root1, root2)));
    }

    @Test
    /**
     * testLeafHelperHasDissociate方法。
     */
    public void testLeafHelperHasDissociate() {
        LeafWrapper<String, String, String> root1 = new LeafWrapper<>("root1", null, "root1");
        LeafWrapper<String, String, String> root2 = new LeafWrapper<>("root2", null, "root2");

        assertTrue(LeafHelper.hasDissociateLeaf(java.util.Arrays.asList(root1, root2)));
        assertFalse(LeafHelper.hasDissociateLeaf(java.util.Arrays.asList(root1)));
    }

    @Test
    /**
     * testLeafHelperWrapper方法。
     */
    public void testLeafHelperWrapper() {
        LeafWrapper<String, String, String> wrapper = LeafHelper.wrapper("1", null, "data");
        assertEquals("1", wrapper.getId());
        assertEquals("data", wrapper.getBean());
    }

    @Test
    /**
     * testLeafHelperTreeify方法。
     */
    public void testLeafHelperTreeify() {
        List<LeafWrapper<String, String, String>> wrappers = new java.util.ArrayList<>();
        wrappers.add(new LeafWrapper<>("root", null, "root"));
        wrappers.add(new LeafWrapper<>("child1", "root", "child1"));
        wrappers.add(new LeafWrapper<>("child2", "root", "child2"));

        LeafWrapper<String, String, String> root = LeafHelper.treeify(wrappers);

        assertEquals("root", root.getName());
        assertEquals(2, root.getChildCount());
    }
}
