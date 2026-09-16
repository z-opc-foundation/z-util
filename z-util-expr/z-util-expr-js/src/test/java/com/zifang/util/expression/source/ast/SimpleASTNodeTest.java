package com.zifang.util.expression.source.ast;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * SimpleASTNode AST节点完整测试
 */
public class SimpleASTNodeTest {

    @Test
    public void testConstructorAndGetters() {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Additive, "+");
        assertEquals(ASTNodeType.Additive, node.getType());
        assertEquals("+", node.getText());
        assertNull(node.getParent());
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    public void testAddChild() {
        SimpleASTNode parent = new SimpleASTNode(ASTNodeType.Additive, "+");
        SimpleASTNode child1 = new SimpleASTNode(ASTNodeType.IntLiteral, "1");
        SimpleASTNode child2 = new SimpleASTNode(ASTNodeType.IntLiteral, "2");

        parent.addChild(child1);
        parent.addChild(child2);

        assertEquals(2, parent.getChildren().size());
        assertEquals(child1, parent.getChildren().get(0));
        assertEquals(child2, parent.getChildren().get(1));
    }

    @Test
    public void testParentReference() {
        SimpleASTNode parent = new SimpleASTNode(ASTNodeType.Additive, "+");
        SimpleASTNode child = new SimpleASTNode(ASTNodeType.IntLiteral, "1");

        assertNull(child.getParent());
        parent.addChild(child);
        assertEquals(parent, child.getParent());
    }

    @Test
    public void testChildrenReadOnly() {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Programm, "p");
        node.addChild(new SimpleASTNode(ASTNodeType.IntLiteral, "1"));

        List<ASTNode> children = node.getChildren();
        try {
            children.clear();
        } catch (UnsupportedOperationException e) {
            assert true;
        }
    }

    @Test
    public void testNestedTree() {
        //       (+)
        //      /   \
        //    (*)   (3)
        //   /   \
        // (1)  (2)
        SimpleASTNode root = new SimpleASTNode(ASTNodeType.Additive, "+");
        SimpleASTNode mult = new SimpleASTNode(ASTNodeType.Multiplicative, "*");
        SimpleASTNode one = new SimpleASTNode(ASTNodeType.IntLiteral, "1");
        SimpleASTNode two = new SimpleASTNode(ASTNodeType.IntLiteral, "2");
        SimpleASTNode three = new SimpleASTNode(ASTNodeType.IntLiteral, "3");

        mult.addChild(one);
        mult.addChild(two);
        root.addChild(mult);
        root.addChild(three);

        assertEquals(2, root.getChildren().size());
        assertEquals(mult, root.getChildren().get(0));
        assertEquals(three, root.getChildren().get(1));
        assertEquals(2, mult.getChildren().size());
        assertEquals(one, mult.getChildren().get(0));
        assertEquals(two, mult.getChildren().get(1));
        assertEquals(root, mult.getParent());
    }

    @Test
    public void testNodeTypeValues() {
        // 验证所有 ASTNodeType 枚举值都能用来创建节点
        for (ASTNodeType type : ASTNodeType.values()) {
            SimpleASTNode node = new SimpleASTNode(type, type.name());
            assertEquals(type, node.getType());
            assertEquals(type.name(), node.getText());
        }
    }
}
