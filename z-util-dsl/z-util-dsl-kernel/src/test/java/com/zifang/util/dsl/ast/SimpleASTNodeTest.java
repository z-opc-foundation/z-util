package com.zifang.util.dsl.ast;

import com.zifang.util.dsl.core.ASTNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleASTNodeTest类。
 */
public class SimpleASTNodeTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        SimpleASTNode node = new SimpleASTNode();
        assertNotNull(node);
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    /**
     * testParameterizedConstructor方法。
     */
    public void testParameterizedConstructor() {
        SimpleASTNode node = new SimpleASTNode("type", "text", 1, 2);
        assertEquals("type", node.getType());
        assertEquals("text", node.getText());
        assertEquals(1, node.getLine());
        assertEquals(2, node.getColumn());
    }

    @Test
    /**
     * testGetSetType方法。
     */
    public void testGetSetType() {
        SimpleASTNode node = new SimpleASTNode();
        node.setType("testType");
        assertEquals("testType", node.getType());
    }

    @Test
    /**
     * testGetSetText方法。
     */
    public void testGetSetText() {
        SimpleASTNode node = new SimpleASTNode();
        node.setText("testText");
        assertEquals("testText", node.getText());
    }

    @Test
    /**
     * testGetSetLine方法。
     */
    public void testGetSetLine() {
        SimpleASTNode node = new SimpleASTNode();
        node.setLine(10);
        assertEquals(10, node.getLine());
    }

    @Test
    /**
     * testGetSetColumn方法。
     */
    public void testGetSetColumn() {
        SimpleASTNode node = new SimpleASTNode();
        node.setColumn(20);
        assertEquals(20, node.getColumn());
    }

    @Test
    /**
     * testGetChildren方法。
     */
    public void testGetChildren() {
        SimpleASTNode parent = new SimpleASTNode();
        SimpleASTNode child1 = new SimpleASTNode("child1", "text1", 1, 1);
        SimpleASTNode child2 = new SimpleASTNode("child2", "text2", 2, 2);

        parent.addChild(child1);
        parent.addChild(child2);

        List<ASTNode> children = parent.getChildren();
        assertEquals(2, children.size());
        assertSame(child1, children.get(0));
        assertSame(child2, children.get(1));
    }

    @Test
    /**
     * testAddChild_SetsParent方法。
     */
    public void testAddChild_SetsParent() {
        SimpleASTNode parent = new SimpleASTNode();
        SimpleASTNode child = new SimpleASTNode();

        parent.addChild(child);

        assertSame(parent, child.getParent());
    }

    @Test
    /**
     * testGetParent方法。
     */
    public void testGetParent() {
        SimpleASTNode parent = new SimpleASTNode();
        SimpleASTNode child = new SimpleASTNode();

        parent.addChild(child);

        assertSame(parent, child.getParent());
    }

    @Test
    /**
     * testSetParent方法。
     */
    public void testSetParent() {
        SimpleASTNode parent = new SimpleASTNode();
        SimpleASTNode child = new SimpleASTNode();

        child.setParent(parent);

        assertSame(parent, child.getParent());
    }

    @Test
    /**
     * testGetSetToken方法。
     */
    public void testGetSetToken() {
        SimpleASTNode node = new SimpleASTNode();
        Object token = new Object();

        node.setToken(token);

        assertSame(token, node.getToken());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        SimpleASTNode node = new SimpleASTNode("type", "text", 1, 2);
        String str = node.toString();

        assertTrue(str.contains("type"));
        assertTrue(str.contains("text"));
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
    }

    @Test
    /**
     * testDump方法。
     */
    public void testDump() {
        SimpleASTNode root = new SimpleASTNode("root", null, 1, 1);
        SimpleASTNode child1 = new SimpleASTNode("child1", "val1", 2, 1);
        SimpleASTNode child2 = new SimpleASTNode("child2", "val2", 3, 1);

        root.addChild(child1);
        root.addChild(child2);

        StringBuilder sb = new StringBuilder();
        root.dump(sb, 0);

        String output = sb.toString();
        assertTrue(output.contains("root"));
        assertTrue(output.contains("child1"));
        assertTrue(output.contains("child2"));
        assertTrue(output.contains("val1"));
        assertTrue(output.contains("val2"));
    }

    @Test
    /**
     * testDump_WithIndent方法。
     */
    public void testDump_WithIndent() {
        SimpleASTNode root = new SimpleASTNode("root", null, 1, 1);
        SimpleASTNode child = new SimpleASTNode("child", "value", 2, 1);

        root.addChild(child);

        StringBuilder sb = new StringBuilder();
        root.dump(sb, 0);

        String output = sb.toString();
        // Child should appear after root with indentation
        assertTrue(output.contains("  child")); // 2 spaces indent
    }

    @Test
    /**
     * testNestedChildren方法。
     */
    public void testNestedChildren() {
        SimpleASTNode root = new SimpleASTNode("root", null, 1, 1);
        SimpleASTNode level1 = new SimpleASTNode("level1", null, 2, 1);
        SimpleASTNode level2 = new SimpleASTNode("level2", null, 3, 1);

        root.addChild(level1);
        level1.addChild(level2);

        assertEquals(1, root.getChildren().size());
        assertEquals(1, level1.getChildren().size());
        assertSame(level2, level1.getChildren().get(0));
        assertSame(level1, level2.getParent());
    }

    @Test
    /**
     * testNullTextHandling方法。
     */
    public void testNullTextHandling() {
        SimpleASTNode node = new SimpleASTNode("type", null, 1, 1);
        assertNull(node.getText());

        StringBuilder sb = new StringBuilder();
        node.dump(sb, 0);
        // Should not throw exception
        assertNotNull(sb.toString());
    }

    @Test
    /**
     * testEmptyTextHandling方法。
     */
    public void testEmptyTextHandling() {
        SimpleASTNode node = new SimpleASTNode("type", "", 1, 1);
        assertEquals("", node.getText());

        StringBuilder sb = new StringBuilder();
        node.dump(sb, 0);
        // Should not throw exception
        assertNotNull(sb.toString());
    }

    @Test
    /**
     * testChildrenReadOnly方法。
     */
    public void testChildrenReadOnly() {
        SimpleASTNode node = new SimpleASTNode();
        node.addChild(new SimpleASTNode("child", "1", 1, 1));
        List<ASTNode> children = node.getChildren();
        assertThrows(UnsupportedOperationException.class, children::clear);
    }
}
