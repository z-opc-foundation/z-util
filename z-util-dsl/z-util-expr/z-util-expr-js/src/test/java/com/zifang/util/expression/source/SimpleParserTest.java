package com.zifang.util.expression.source;

import com.zifang.util.expression.source.ast.ASTNode;
import com.zifang.util.expression.source.ast.ASTNodeType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * SimpleParser 语法解析器完整测试
 */
public class SimpleParserTest {

    // ==================== 基础解析测试 ====================

    @Test
    public void testParseIntDeclaration() throws Exception {
        SimpleParser parser = new SimpleParser();
        String script = "int age = 45;";

        ASTNode tree = parser.parse(script);

        assertNotNull(tree);
        assertEquals(ASTNodeType.Programm, tree.getType());
        assertEquals(1, tree.getChildren().size());

        ASTNode intDecl = tree.getChildren().get(0);
        assertEquals(ASTNodeType.IntDeclaration, intDecl.getType());
        assertEquals("age", intDecl.getText());
    }

    @Test
    public void testParseIntDeclarationWithoutInitializer() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("int x;");

        ASTNode intDecl = tree.getChildren().get(0);
        assertEquals(ASTNodeType.IntDeclaration, intDecl.getType());
        assertEquals("x", intDecl.getText());
        assertEquals(0, intDecl.getChildren().size());
    }

    @Test
    public void testParseIntDeclarationWithExpression() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("int a = 10+20;");

        ASTNode intDecl = tree.getChildren().get(0);
        assertEquals(ASTNodeType.IntDeclaration, intDecl.getType());
        assertEquals(1, intDecl.getChildren().size());
    }

    @Test
    public void testParseAssignmentStatement() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("age = 20;");

        ASTNode assign = tree.getChildren().get(0);
        assertEquals(ASTNodeType.AssignmentStmt, assign.getType());
        assertEquals("age", assign.getText());
        assertEquals(1, assign.getChildren().size());
    }

    @Test
    public void testParseExpressionStatement() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("age+10*2;");

        ASTNode expr = tree.getChildren().get(0);
        // expression statement 的结果是表达式本身的根节点
        assertNotNull(expr);
    }

    // ==================== 多语句解析测试 ====================

    @Test
    public void testParseMultipleStatements() throws Exception {
        SimpleParser parser = new SimpleParser();
        String script = "int age = 45+2; age= 20; age+10*2;";

        ASTNode tree = parser.parse(script);

        assertEquals(3, tree.getChildren().size());
        assertEquals(ASTNodeType.IntDeclaration, tree.getChildren().get(0).getType());
        assertEquals(ASTNodeType.AssignmentStmt, tree.getChildren().get(1).getType());
        // 第三个是表达式语句
    }

    @Test
    public void testParseMultipleDeclarations() throws Exception {
        SimpleParser parser = new SimpleParser();
        String script = "int a = 10; int b = 20;";

        ASTNode tree = parser.parse(script);

        assertEquals(2, tree.getChildren().size());
        assertEquals("a", tree.getChildren().get(0).getText());
        assertEquals("b", tree.getChildren().get(1).getText());
    }

    @Test
    public void testParseMixedStatements() throws Exception {
        SimpleParser parser = new SimpleParser();
        String script = "int x = 5; x = x + 1; x;";

        ASTNode tree = parser.parse(script);

        assertEquals(3, tree.getChildren().size());
        assertEquals(ASTNodeType.IntDeclaration, tree.getChildren().get(0).getType());
        assertEquals(ASTNodeType.AssignmentStmt, tree.getChildren().get(1).getType());
    }

    // ==================== 表达式解析测试 ====================

    @Test
    public void testParseSimpleAddition() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("a + b;");

        ASTNode addNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, addNode.getType());
        assertEquals("+", addNode.getText());
    }

    @Test
    public void testParseSimpleSubtraction() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("a - b;");

        ASTNode subNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, subNode.getType());
        assertEquals("-", subNode.getText());
    }

    @Test
    public void testParseMultiplication() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("a * b;");

        ASTNode mulNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Multiplicative, mulNode.getType());
        assertEquals("*", mulNode.getText());
    }

    @Test
    public void testParseDivision() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("a / b;");

        ASTNode divNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Multiplicative, divNode.getType());
        assertEquals("/", divNode.getText());
    }

    @Test
    public void testParseComplexArithmetic() throws Exception {
        SimpleParser parser = new SimpleParser();
        String script = "int result = a + b * c - d / e;";

        ASTNode tree = parser.parse(script);

        assertNotNull(tree);
        ASTNode intDecl = tree.getChildren().get(0);
        assertEquals(ASTNodeType.IntDeclaration, intDecl.getType());
        assertEquals("result", intDecl.getText());
    }

    @Test
    public void testParseParenthesizedExpression() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("(a + b);");

        ASTNode addNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, addNode.getType());
    }

    @Test
    public void testParseIntLiteral() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("123;");

        ASTNode litNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.IntLiteral, litNode.getType());
        assertEquals("123", litNode.getText());
    }

    @Test
    public void testParseIdentifier() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("myVariable;");

        ASTNode idNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Identifier, idNode.getType());
        assertEquals("myVariable", idNode.getText());
    }

    @Test
    public void testParseComplexExpression() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("2+3*5;");

        ASTNode node = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, node.getType());
    }

    // ==================== AST 结构验证 ====================

    @Test
    public void testParseASTTreeStructure() throws Exception {
        // 验证 1+2*3 的AST结构
        // additive: 1 + (multiplicative: 2 * 3)
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("1+2*3;");

        ASTNode root = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, root.getType());
        assertEquals("+", root.getText());
        assertEquals(2, root.getChildren().size());

        ASTNode left = root.getChildren().get(0);
        assertEquals(ASTNodeType.IntLiteral, left.getType());
        assertEquals("1", left.getText());

        ASTNode right = root.getChildren().get(1);
        assertEquals(ASTNodeType.Multiplicative, right.getType());
        assertEquals("*", right.getText());
    }

    @Test
    public void testParseNestedParentheses() throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("((1+2)*3);");

        assertNotNull(tree);
        assertEquals(1, tree.getChildren().size());
    }

    // ==================== 异常解析测试 ====================

    @Test(expected = Exception.class)
    public void testParseInvalidSyntaxMissingSemicolon() throws Exception {
        SimpleParser parser = new SimpleParser();
        parser.parse("2+3+;");
    }

    @Test(expected = Exception.class)
    public void testParseInvalidSyntaxIncompleteExpression() throws Exception {
        SimpleParser parser = new SimpleParser();
        parser.parse("2+3*;");
    }

    @Test(expected = Exception.class)
    public void testParseInvalidSyntaxMissingVariableName() throws Exception {
        SimpleParser parser = new SimpleParser();
        parser.parse("int ;");
    }

    @Test(expected = Exception.class)
    public void testParseInvalidSyntaxMissingSemicolonAfterDeclaration() throws Exception {
        SimpleParser parser = new SimpleParser();
        parser.parse("int a = 10");
    }

    @Test(expected = Exception.class)
    public void testParseUnknownStatement() throws Exception {
        SimpleParser parser = new SimpleParser();
        parser.parse("unknown statement");
    }

    // ==================== 边界测试 ====================

    @Test
    public void testParseEmptyProgram() throws Exception {
        SimpleParser parser = new SimpleParser();
        try {
            ASTNode tree = parser.parse("");
            // 空输入可能返回空tree或null
            if (tree != null) {
                assertEquals(ASTNodeType.Programm, tree.getType());
            }
        } catch (Exception e) {
            // 也允许抛异常
        }
    }

    @Test
    public void testParseAssignmentWithoutPriorDeclaration() throws Exception {
        // SimpleScript 会报错，但 SimpleParser 不检查变量声明
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("age = 20;");
        assertEquals(1, tree.getChildren().size());
        assertEquals(ASTNodeType.AssignmentStmt, tree.getChildren().get(0).getType());
    }

    @Test
    public void testParseOperatorPrecedenceInAST() throws Exception {
        // 验证 AST 结构反映了优先级: 2+3*4 应该是 additive(multiplicative(2,3), 4) 还是 additive(2, multiplicative(3,4))
        // 期望后者，即乘法作为加法的右操作数
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("2+3*4;");

        ASTNode addNode = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, addNode.getType());

        ASTNode left = addNode.getChildren().get(0);
        ASTNode right = addNode.getChildren().get(1);

        assertEquals(ASTNodeType.IntLiteral, left.getType());
        assertEquals(ASTNodeType.Multiplicative, right.getType());
    }
}
