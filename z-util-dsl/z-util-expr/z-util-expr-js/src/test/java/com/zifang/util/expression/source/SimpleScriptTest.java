package com.zifang.util.expression.source;

import com.zifang.util.expression.source.ast.ASTNode;
import com.zifang.util.expression.source.ast.ASTNodeType;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * SimpleScript 脚本解释器完整测试
 * 通过反射调用 evaluate 方法，测试变量表和表达式求值
 */
public class SimpleScriptTest {

    /**
     * 通过反射调用 SimpleScript 的 private evaluate 方法
     */
    private Integer evaluate(SimpleScript script, String expr) throws Exception {
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse(expr);
        Method evaluateMethod = SimpleScript.class.getDeclaredMethod("evaluate", ASTNode.class, String.class);
        evaluateMethod.setAccessible(true);
        return (Integer) evaluateMethod.invoke(script, tree, "");
    }

    @Test
    public void testSimpleAddition() throws Exception {
        SimpleScript script = new SimpleScript();
        Integer result = evaluate(script, "2+3;");
        assertEquals(Integer.valueOf(5), result);
    }

    @Test
    public void testSimpleMultiplication() throws Exception {
        SimpleScript script = new SimpleScript();
        Integer result = evaluate(script, "2*3;");
        assertEquals(Integer.valueOf(6), result);
    }

    @Test
    public void testDivision() throws Exception {
        SimpleScript script = new SimpleScript();
        Integer result = evaluate(script, "10/2;");
        assertEquals(Integer.valueOf(5), result);
    }

    @Test
    public void testSubtraction() throws Exception {
        SimpleScript script = new SimpleScript();
        Integer result = evaluate(script, "10-3;");
        assertEquals(Integer.valueOf(7), result);
    }

    @Test
    public void testOperatorPrecedence() throws Exception {
        SimpleScript script = new SimpleScript();
        // 乘法优先级高于加法: 2 + 3 * 4 = 2 + 12 = 14
        Integer result = evaluate(script, "2+3*4;");
        assertEquals(Integer.valueOf(14), result);
    }

    @Test
    public void testParenthesesOverridePrecedence() throws Exception {
        SimpleScript script = new SimpleScript();
        // (2 + 3) * 4 = 5 * 4 = 20
        Integer result = evaluate(script, "(2+3)*4;");
        assertEquals(Integer.valueOf(20), result);
    }

    @Test
    public void testNestedParentheses() throws Exception {
        SimpleScript script = new SimpleScript();
        // ((1+2)) = 3
        Integer result = evaluate(script, "((1+2));");
        assertEquals(Integer.valueOf(3), result);
    }

    @Test
    public void testIntDeclaration() throws Exception {
        SimpleScript script = new SimpleScript();
        ASTNode tree = new SimpleParser().parse("int a = 10;");
        Method evaluateMethod = SimpleScript.class.getDeclaredMethod("evaluate", ASTNode.class, String.class);
        evaluateMethod.setAccessible(true);
        Integer result = (Integer) evaluateMethod.invoke(script, tree, "");
        assertEquals(Integer.valueOf(10), result);
    }

    @Test
    public void testAssignment() throws Exception {
        SimpleScript script = new SimpleScript();
        // 先声明再赋值
        new SimpleParser().parse("int a = 5;");
        Method evalMethod = SimpleScript.class.getDeclaredMethod("evaluate", ASTNode.class, String.class);
        evalMethod.setAccessible(true);

        // evaluate int declaration
        ASTNode decl = new SimpleParser().parse("int a = 5;");
        evalMethod.invoke(script, decl, "");

        // evaluate assignment
        ASTNode assign = new SimpleParser().parse("a = 20;");
        Integer result = (Integer) evalMethod.invoke(script, assign, "");
        assertEquals(Integer.valueOf(20), result);
    }

    @Test
    public void testUnknownVariableThrows() throws Exception {
        SimpleScript script = new SimpleScript();
        Method evalMethod = SimpleScript.class.getDeclaredMethod("evaluate", ASTNode.class, String.class);
        evalMethod.setAccessible(true);
        ASTNode tree = new SimpleParser().parse("unknownVar;");

        try {
            evalMethod.invoke(script, tree, "");
            fail("Should throw exception for unknown variable");
        } catch (Exception e) {
            // 期望抛出异常
            assertTrue(e.getCause().getMessage().contains("unknown variable"));
        }
    }

    @Test
    public void testVariableReferencedBeforeAssignment() throws Exception {
        SimpleScript script = new SimpleScript();
        Method evalMethod = SimpleScript.class.getDeclaredMethod("evaluate", ASTNode.class, String.class);
        evalMethod.setAccessible(true);

        // int a = b + 1; 其中 b 未声明
        ASTNode tree = new SimpleParser().parse("int a = b + 1;");
        try {
            evalMethod.invoke(script, tree, "");
            fail("Should throw exception");
        } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("unknown variable"));
        }
    }

    @Test
    public void testMultiStatement() throws Exception {
        SimpleParser parser = new SimpleParser();
        Method evalMethod = SimpleScript.class.getDeclaredMethod("evaluate", ASTNode.class, String.class);
        evalMethod.setAccessible(true);

        SimpleScript script = new SimpleScript();

        // int a = 10;
        ASTNode t1 = parser.parse("int a = 10;");
        evalMethod.invoke(script, t1, "");

        // a = a + 5;
        ASTNode t2 = parser.parse("a = a + 5;");
        Integer result = (Integer) evalMethod.invoke(script, t2, "");
        assertEquals(Integer.valueOf(15), result);
    }

    @Test
    public void testExpressionStatement() throws Exception {
        SimpleScript script = new SimpleScript();
        Integer result = evaluate(script, "100;");
        assertEquals(Integer.valueOf(100), result);
    }

    @Test
    public void testParseComplexExpression() throws Exception {
        // 验证解析器能处理复杂表达式
        SimpleParser parser = new SimpleParser();
        ASTNode tree = parser.parse("int result = a + b * c - d / e;");
        assertNotNull(tree);
        assertEquals(ASTNodeType.Programm, tree.getType());
        assertFalse(tree.getChildren().isEmpty());
    }

    @Test
    public void testEvaluateComplexArithmetic() throws Exception {
        SimpleScript script = new SimpleScript();
        // 验证运算顺序
        // 8 / 2 * 4 = 4 * 4 = 16
        Integer result = evaluate(script, "8/2*4;");
        assertEquals(Integer.valueOf(16), result);
    }
}
