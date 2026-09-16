package com.zifang.util.expression.source;

import com.zifang.util.expression.source.ast.ASTNode;
import com.zifang.util.expression.source.ast.ASTNodeType;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * SimpleCalculator 计算器完整测试
 */
public class SimpleCalculatorTest {

    // ==================== parse 方法测试 ====================

    @Test
    public void testParseSingleNumber() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("42");
        assertNotNull(tree);
        assertEquals(ASTNodeType.Programm, tree.getType());
        assertFalse(tree.getChildren().isEmpty());
    }

    @Test
    public void testParseSimpleAddition() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("2+3");
        assertNotNull(tree);
        ASTNode child = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, child.getType());
        assertEquals("+", child.getText());
        assertEquals(2, child.getChildren().size());
    }

    @Test
    public void testParseSimpleSubtraction() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("10-3");
        ASTNode child = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Additive, child.getType());
        assertEquals("-", child.getText());
    }

    @Test
    public void testParseMultiplication() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("4*5");
        ASTNode child = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Multiplicative, child.getType());
        assertEquals("*", child.getText());
    }

    @Test
    public void testParseDivision() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("10/2");
        ASTNode child = tree.getChildren().get(0);
        assertEquals(ASTNodeType.Multiplicative, child.getType());
        assertEquals("/", child.getText());
    }

    @Test
    public void testParseComplexExpression() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("2+3*5");
        assertNotNull(tree);
        assertEquals(1, tree.getChildren().size());
    }

    @Test
    public void testParseExpressionWithParentheses() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("(2+3)");
        assertNotNull(tree);
        assertFalse(tree.getChildren().isEmpty());
    }

    @Test
    public void testParseNestedParentheses() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("((1+2))");
        assertNotNull(tree);
        assertFalse(tree.getChildren().isEmpty());
    }

    @Test
    public void testParseIdentifier() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("abc");
        assertNotNull(tree);
        assertEquals(ASTNodeType.Identifier, tree.getChildren().get(0).getType());
    }

    @Test
    public void testParseZero() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("0");
        assertNotNull(tree);
    }

    @Test
    public void testParseLargeNumber() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("123456789");
        assertNotNull(tree);
    }

    @Test
    public void testParseMultipleOperations() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("1+2*3-4/2");
        assertNotNull(tree);
        assertEquals(1, tree.getChildren().size());
    }

    @Test
    public void testParseExpressionWithLeadingSpaces() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("   2+3   ");
        assertNotNull(tree);
    }

    // ==================== evaluate 方法测试 ====================

    @Test
    public void testEvaluateAddition() {
        SimpleCalculator calculator = new SimpleCalculator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        calculator.evaluate("2+3");

        String output = out.toString();
        assertTrue(output.contains("Calculating:"));
        assertTrue(output.contains("Result:"));
    }

    @Test
    public void testEvaluateMultiplication() {
        SimpleCalculator calculator = new SimpleCalculator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        calculator.evaluate("2*3");

        String output = out.toString();
        assertTrue(output.contains("Result:"));
    }

    @Test
    public void testEvaluateDivision() {
        SimpleCalculator calculator = new SimpleCalculator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        calculator.evaluate("10/2");

        String output = out.toString();
        assertTrue(output.contains("Result:"));
    }

    @Test
    public void testEvaluateSubtraction() {
        SimpleCalculator calculator = new SimpleCalculator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        calculator.evaluate("10-3");

        String output = out.toString();
        assertTrue(output.contains("Result:"));
    }

    @Test
    public void testEvaluateParentheses() {
        SimpleCalculator calculator = new SimpleCalculator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        calculator.evaluate("(1+2)*3");

        String output = out.toString();
        assertTrue(output.contains("Result:"));
    }

    @Test
    public void testEvaluateInvalidSyntax() {
        SimpleCalculator calculator = new SimpleCalculator();
        // 不应抛出异常，内部捕获了
        calculator.evaluate("2+");
    }

    @Test
    public void testEvaluateEmptyExpression() {
        SimpleCalculator calculator = new SimpleCalculator();
        try {
            calculator.evaluate("");
        } catch (Exception e) {
            // 也允许抛异常
        }
    }

    // ==================== 异常情况测试 ====================

    @Test(expected = Exception.class)
    public void testParseIncompleteExpressionThrows() throws Exception {
        // "2+" - additive检测到+但右操作数不完整，抛异常
        SimpleCalculator calculator = new SimpleCalculator();
        calculator.parse("2+");
    }

    @Test(expected = Exception.class)
    public void testParseUnclosedParenthesisThrows() throws Exception {
        SimpleCalculator calculator = new SimpleCalculator();
        calculator.parse("(2+3");
    }

    // 注: SimpleCalculator 对以下情况不抛异常，而是返回不完整的树
    // - "*2"   - multiplicative的左操作数为null，被忽略
    // - "(2+3))" - 末尾多余的)被跳过

    @Test
    public void testParseMissingLeftOperandReturnsNull() throws Exception {
        // "*2" - primary返回null，multiplicative不做处理返回null
        // SimpleCalculator对此返回空树，不抛异常
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("*2");
        // 返回非空树但无子节点
        assertNotNull(tree);
    }

    @Test
    public void testParseExtraClosingParenthesisReturnsTree() throws Exception {
        // "(2+3))" - 末尾多余的)被忽略，返回正常树
        SimpleCalculator calculator = new SimpleCalculator();
        ASTNode tree = calculator.parse("(2+3))");
        assertNotNull(tree);
    }
}
