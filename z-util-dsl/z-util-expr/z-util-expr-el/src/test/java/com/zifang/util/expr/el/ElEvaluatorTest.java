package com.zifang.util.expr.el;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for ElEvaluator.
 */
public class ElEvaluatorTest {

    private ElEvaluator evaluator;

    @Before
    public void setUp() {
        evaluator = new ElEvaluator();
    }

    @After
    public void tearDown() {
        evaluator = null;
    }

    // ==================== Arithmetic Expression Tests ====================

    @Test
    public void testEvalSimpleAddition() {
        Object result = evaluator.eval("1 + 2");
        assertEquals(3, result);
    }

    @Test
    public void testEvalSimpleSubtraction() {
        Object result = evaluator.eval("10 - 3");
        assertEquals(7, result);
    }

    @Test
    public void testEvalMultiplication() {
        Object result = evaluator.eval("4 * 5");
        assertEquals(20, result);
    }

    @Test
    public void testEvalDivision() {
        Object result = evaluator.eval("10 / 2");
        // SpEL returns integer division result when both operands are integers
        assertEquals(5, ((Number) result).intValue());
    }

    @Test
    public void testEvalModulo() {
        Object result = evaluator.eval("10 % 3");
        assertEquals(1, result);
    }

    @Test
    public void testEvalComplexArithmetic() {
        Object result = evaluator.eval("(1 + 2) * 3 - 6 / 2");
        assertEquals(6, ((Number) result).intValue());
    }

    @Test
    public void testEvalNegativeNumbers() {
        Object result = evaluator.eval("-5 + 10");
        assertEquals(5, result);
    }

    // ==================== Comparison Expression Tests ====================

    @Test
    public void testEvalEqual() {
        Object result = evaluator.eval("1 == 1");
        assertEquals(true, result);
    }

    @Test
    public void testEvalNotEqual() {
        Object result = evaluator.eval("1 != 2");
        assertEquals(true, result);
    }

    @Test
    public void testEvalGreaterThan() {
        Object result = evaluator.eval("5 > 3");
        assertEquals(true, result);
    }

    @Test
    public void testEvalLessThan() {
        Object result = evaluator.eval("3 < 5");
        assertEquals(true, result);
    }

    @Test
    public void testEvalGreaterThanOrEqual() {
        Object result = evaluator.eval("5 >= 5");
        assertEquals(true, result);
    }

    @Test
    public void testEvalLessThanOrEqual() {
        Object result = evaluator.eval("3 <= 3");
        assertEquals(true, result);
    }

    @Test
    public void testEvalComparisonWithVariables() {
        evaluator.setVariable("a", 10);
        evaluator.setVariable("b", 20);
        // SpEL requires # prefix for variables
        Object result = evaluator.eval("#a < #b");
        assertEquals(true, result);
    }

    // ==================== Logical Expression Tests ====================

    @Test
    public void testEvalLogicalAnd() {
        Object result = evaluator.eval("true && true");
        assertEquals(true, result);
    }

    @Test
    public void testEvalLogicalAndFalse() {
        Object result = evaluator.eval("true && false");
        assertEquals(false, result);
    }

    @Test
    public void testEvalLogicalOr() {
        Object result = evaluator.eval("false || true");
        assertEquals(true, result);
    }

    @Test
    public void testEvalLogicalOrBothFalse() {
        Object result = evaluator.eval("false || false");
        assertEquals(false, result);
    }

    @Test
    public void testEvalLogicalNot() {
        Object result = evaluator.eval("!true");
        assertEquals(false, result);
    }

    @Test
    public void testEvalComplexLogical() {
        Object result = evaluator.eval("(1 < 2) && (3 > 2)");
        assertEquals(true, result);
    }

    // ==================== Ternary Conditional Tests ====================

    @Test
    public void testEvalTernaryTrue() {
        Object result = evaluator.eval("true ? 1 : 2");
        assertEquals(1, result);
    }

    @Test
    public void testEvalTernaryFalse() {
        Object result = evaluator.eval("false ? 1 : 2");
        assertEquals(2, result);
    }

    @Test
    public void testEvalTernaryWithComparison() {
        Object result = evaluator.eval("5 > 3 ? 'yes' : 'no'");
        assertEquals("yes", result);
    }

    @Test
    public void testEvalNestedTernary() {
        Object result = evaluator.eval("true ? (false ? 1 : 2) : 3");
        assertEquals(2, result);
    }

    // ==================== Property Access Tests ====================

    @Test
    public void testEvalPropertyAccess() {
        TestBean bean = new TestBean();
        bean.setName("test");
        bean.setAge(25);
        Object result = evaluator.eval("name", bean);
        assertEquals("test", result);
    }

    @Test
    public void testEvalNestedPropertyAccess() {
        TestBean outer = new TestBean();
        TestBean inner = new TestBean();
        inner.setName("nested");
        outer.setChild(inner);
        Object result = evaluator.eval("child.name", outer);
        assertEquals("nested", result);
    }

    // ==================== Map Access Tests ====================

    @Test
    public void testEvalMapAccess() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("number", 42);
        evaluator.setVariable("map", map);
        // SpEL uses #variableName for accessing variables, and ['key'] for map access
        Object result = evaluator.eval("#map['key']");
        assertEquals("value", result);
    }

    // ==================== List Access Tests ====================

    @Test
    public void testEvalListAccess() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        evaluator.setVariable("list", list);
        Object result = evaluator.eval("#list[0]");
        assertEquals(1, result);
    }

    @Test
    public void testEvalListAccessWithIndex() {
        List<String> list = Arrays.asList("a", "b", "c");
        evaluator.setVariable("list", list);
        Object result = evaluator.eval("#list[2]");
        assertEquals("c", result);
    }

    // ==================== Empty Operator Tests ====================

    @Test
    public void testEvalEmptyWithNull() {
        evaluator.setVariable("nullVal", null);
        Object result = evaluator.eval("#nullVal == null");
        assertEquals(true, result);
    }

    @Test
    public void testEvalEmptyWithEmptyString() {
        Object result = evaluator.eval("''.empty");
        assertEquals(true, result);
    }

    @Test
    public void testEvalEmptyWithEmptyList() {
        List<Object> emptyList = Arrays.asList();
        evaluator.setVariable("emptyList", emptyList);
        Object result = evaluator.eval("#emptyList.size() == 0");
        assertEquals(true, result);
    }

    @Test
    public void testEvalEmptyWithNonEmptyList() {
        List<Integer> nonEmptyList = Arrays.asList(1, 2, 3);
        evaluator.setVariable("nonEmptyList", nonEmptyList);
        Object result = evaluator.eval("#nonEmptyList.size() > 0");
        assertEquals(true, result);
    }

    @Test
    public void testEvalEmptyWithNonEmptyString() {
        Object result = evaluator.eval("'hello'.empty == false");
        assertEquals(true, result);
    }

    // ==================== Method Call Tests ====================

    @Test
    public void testEvalMethodCall() {
        TestBean bean = new TestBean();
        bean.setName("test");
        Object result = evaluator.eval("getName()", bean);
        assertEquals("test", result);
    }

    @Test
    public void testEvalStringMethodCall() {
        Object result = evaluator.eval("'hello'.toUpperCase()");
        assertEquals("HELLO", result);
    }

    @Test
    public void testEvalStringLength() {
        Object result = evaluator.eval("'hello'.length()");
        assertEquals(5, result);
    }

    // ==================== Exception Tests ====================

    @Test(expected = ElException.class)
    public void testEvalInvalidExpression() {
        evaluator.eval("1 +");
    }

    @Test(expected = ElException.class)
    public void testEvalUndefinedProperty() {
        TestBean bean = new TestBean();
        evaluator.eval("undefinedProperty", bean);
    }

    @Test
    public void testElExceptionContainsMessage() {
        try {
            evaluator.eval("1 +");
            fail("Expected ElException");
        } catch (ElException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Failed to evaluate"));
        }
    }

    @Test
    public void testElExceptionContainsCause() {
        try {
            evaluator.eval("1 +");
            fail("Expected ElException");
        } catch (ElException e) {
            assertNotNull(e.getCause());
        }
    }

    // ==================== Variable Tests ====================

    @Test
    public void testSetAndGetVariable() {
        evaluator.setVariable("x", 10);
        Object result = evaluator.eval("#x + 5");
        assertEquals(15, result);
    }

    @Test
    public void testMultipleVariables() {
        evaluator.setVariable("a", 3);
        evaluator.setVariable("b", 4);
        Object result = evaluator.eval("#a * #b");
        assertEquals(12, result);
    }

    @Test
    public void testEvalWithVariablesMap() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 5);
        variables.put("b", 3);
        Object result = evaluator.eval("#a + #b", variables);
        assertEquals(8, result);
    }

    @Test
    public void testVariableIsolation() {
        evaluator.setVariable("x", 100);
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", 200);
        evaluator.eval("#x", variables);
        assertEquals(100, evaluator.getVariable("x"));
    }

    @Test
    public void testClearVariables() {
        evaluator.setVariable("x", 10);
        assertEquals(10, evaluator.getVariable("x"));
        evaluator.clearVariables();
        assertNull(evaluator.getVariable("x"));
    }

    // ==================== Edge Cases ====================

    @Test
    public void testEvalNullVariable() {
        evaluator.setVariable("nullVar", null);
        Object result = evaluator.eval("#nullVar");
        assertNull(result);
    }

    @Test
    public void testEvalBooleanValues() {
        Object result = evaluator.eval("true");
        assertEquals(true, result);
    }

    @Test
    public void testEvalStringConcat() {
        Object result = evaluator.eval("'Hello ' + 'World'");
        assertEquals("Hello World", result);
    }

    @Test
    public void testEvalComplexExpression() {
        evaluator.setVariable("a", 10);
        evaluator.setVariable("b", 5);
        Object result = evaluator.eval("#a > #b ? #a + #b : #a - #b");
        assertEquals(15, result);
    }

    // ==================== Helper Class ====================

    public static class TestBean {
        private String name;
        private int age;
        private TestBean child;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public TestBean getChild() {
            return child;
        }

        public void setChild(TestBean child) {
            this.child = child;
        }
    }
}
