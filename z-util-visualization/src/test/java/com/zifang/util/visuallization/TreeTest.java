package com.zifang.util.visuallization;

import com.zifang.util.visuallization.swing.manager.tree.RegisterTreeNode;
import com.zifang.util.visuallization.swing.manager.tree.RegisterTreeNodeHelper;
import com.zifang.util.visuallization.swing.manager.tree.TreeNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 树形结构相关类的单元测试
 */
class TreeTest {

    // ==================== TreeNode 测试 ====================

    @Test
    void testTreeNodeDefaultConstructor() {
        TreeNode node = new TreeNode();
        assertNull(node.getId());
        assertNull(node.getName());
        assertNull(node.getParentId());
    }

    @Test
    void testTreeNodeParameterizedConstructor() {
        TreeNode node = new TreeNode("1", "TestNode", "root");
        assertEquals("1", node.getId());
        assertEquals("TestNode", node.getName());
        assertEquals("root", node.getParentId());
    }

    @Test
    void testTreeNodeSetters() {
        TreeNode node = new TreeNode();
        node.setId("2");
        node.setName("NewNode");
        node.setParentId("parent1");
        assertEquals("2", node.getId());
        assertEquals("NewNode", node.getName());
        assertEquals("parent1", node.getParentId());
    }

    @Test
    void testTreeNodeToString() {
        TreeNode node = new TreeNode("1", "TestNode", "root");
        String str = node.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("TestNode"));
        assertTrue(str.contains("root"));
    }

    @Test
    void testTreeNodeEquals() {
        TreeNode node1 = new TreeNode("1", "Test", "root");
        TreeNode node2 = new TreeNode("1", "Test", "root");
        TreeNode node3 = new TreeNode("2", "Test", "root");
        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
    }

    @Test
    void testTreeNodeEqualsSame() {
        TreeNode node = new TreeNode("1", "Test", "root");
        assertEquals(node, node);
    }

    @Test
    void testTreeNodeEqualsNull() {
        TreeNode node = new TreeNode("1", "Test", "root");
        assertNotEquals(node, null);
    }

    @Test
    void testTreeNodeEqualsDifferentClass() {
        TreeNode node = new TreeNode("1", "Test", "root");
        assertNotEquals(node, "not a treenode");
    }

    @Test
    void testTreeNodeHashCode() {
        TreeNode node1 = new TreeNode("1", "Test", "root");
        TreeNode node2 = new TreeNode("1", "Test", "root");
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    // ==================== RegisterTreeNode 测试 ====================

    @Test
    void testRegisterTreeNodeDefaultConstructor() {
        RegisterTreeNode register = new RegisterTreeNode();
        assertNotNull(register.getTreeNodes());
        assertTrue(register.getTreeNodes().isEmpty());
    }

    @Test
    void testRegisterTreeNodeRegister() {
        RegisterTreeNode register = new RegisterTreeNode();
        TreeNode node = new TreeNode("1", "Test", "root");
        RegisterTreeNode result = register.register(node);
        assertSame(register, result);
        assertEquals(1, register.getTreeNodes().size());
    }

    @Test
    void testRegisterTreeNodeSetTreeNodes() {
        RegisterTreeNode register = new RegisterTreeNode();
        java.util.List<TreeNode> nodes = new java.util.ArrayList<>();
        nodes.add(new TreeNode("1", "Node1", null));
        nodes.add(new TreeNode("2", "Node2", "1"));
        register.setTreeNodes(nodes);
        assertEquals(2, register.getTreeNodes().size());
    }

    @Test
    void testRegisterTreeNodeChaining() {
        RegisterTreeNode register = new RegisterTreeNode();
        register.register(new TreeNode("1", "Node1", null))
                .register(new TreeNode("2", "Node2", "1"));
        assertEquals(2, register.getTreeNodes().size());
    }

    @Test
    void testRegisterTreeNodeToString() {
        RegisterTreeNode register = new RegisterTreeNode();
        register.register(new TreeNode("1", "Test", null));
        String str = register.toString();
        assertTrue(str.contains("RegisterTreeNode"));
    }

    @Test
    void testRegisterTreeNodeEquals() {
        RegisterTreeNode reg1 = new RegisterTreeNode();
        RegisterTreeNode reg2 = new RegisterTreeNode();
        assertEquals(reg1, reg2);
    }

    @Test
    void testRegisterTreeNodeEqualsDifferentNodes() {
        RegisterTreeNode reg1 = new RegisterTreeNode();
        reg1.register(new TreeNode("1", "Test", null));
        RegisterTreeNode reg2 = new RegisterTreeNode();
        assertNotEquals(reg1, reg2);
    }

    @Test
    void testRegisterTreeNodeHashCode() {
        RegisterTreeNode reg1 = new RegisterTreeNode();
        RegisterTreeNode reg2 = new RegisterTreeNode();
        assertEquals(reg1.hashCode(), reg2.hashCode());
    }

    // ==================== RegisterTreeNodeHelper 测试 ====================

    @Test
    void testRegisterTreeNodeHelperSolve() {
        RegisterTreeNode register = new RegisterTreeNode();
        register.register(new TreeNode("root", "Root", null))
                .register(new TreeNode("1", "Child1", "root"))
                .register(new TreeNode("2", "Child2", "root"));

        javax.swing.tree.DefaultMutableTreeNode result = RegisterTreeNodeHelper.solve(register);
        assertNotNull(result);
        assertEquals(2, result.getChildCount());
    }

    @Test
    void testRegisterTreeNodeHelperSolveWithGrandchildren() {
        RegisterTreeNode register = new RegisterTreeNode();
        register.register(new TreeNode("root", "Root", null))
                .register(new TreeNode("1", "Child1", "root"))
                .register(new TreeNode("1-1", "Grandchild1", "1"));

        javax.swing.tree.DefaultMutableTreeNode result = RegisterTreeNodeHelper.solve(register);
        assertNotNull(result);
        assertEquals(1, result.getChildCount());
        javax.swing.tree.DefaultMutableTreeNode child = (javax.swing.tree.DefaultMutableTreeNode) result.getChildAt(0);
        assertEquals(1, child.getChildCount());
    }

    @Test
    void testRegisterTreeNodeHelperSolveMultipleRoots() {
        RegisterTreeNode register = new RegisterTreeNode();
        register.register(new TreeNode("1", "Root1", null))
                .register(new TreeNode("2", "Root2", null));

        assertThrows(RuntimeException.class, () -> RegisterTreeNodeHelper.solve(register));
    }

    @Test
    void testRegisterTreeNodeHelperSolveOrphanNodes() {
        // Orphan nodes are ignored in solve, not thrown as exception
        RegisterTreeNode register = new RegisterTreeNode();
        register.register(new TreeNode("root", "Root", null))
                .register(new TreeNode("1", "Orphan", "nonexistent"));

        javax.swing.tree.DefaultMutableTreeNode result = RegisterTreeNodeHelper.solve(register);
        assertNotNull(result);
        // Root node exists, orphan is simply ignored (0 children since "nonexistent" parent doesn't exist)
        assertEquals(0, result.getChildCount());
    }
}