package com.zifang.util.expr.groovy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for GroovyExecutor.
 */
public class GroovyExecutorTest {

    private GroovyExecutor executor;

    @Before
    public void setUp() {
        executor = new GroovyExecutor();
    }

    @After
    public void tearDown() {
        executor = null;
    }

    // ==================== Arithmetic Expression Tests ====================

    @Test
    public void testEvalSimpleAddition() {
        Object result = executor.eval("1 + 2");
        assertEquals(3, result);
    }

    @Test
    public void testEvalSimpleSubtraction() {
        Object result = executor.eval("10 - 3");
        assertEquals(7, result);
    }

    @Test
    public void testEvalMultiplication() {
        Object result = executor.eval("4 * 5");
        assertEquals(20, result);
    }

    @Test
    public void testEvalDivision() {
        Object result = executor.eval("10 / 2");
        assertEquals(5, ((Number) result).intValue());
    }

    @Test
    public void testEvalComplexArithmetic() {
        Object result = executor.eval("(1 + 2) * 3 - 6 / 2");
        assertEquals(6, ((Number) result).intValue());
    }

    @Test
    public void testEvalModulo() {
        Object result = executor.eval("10 % 3");
        assertEquals(1, result);
    }

    @Test
    public void testEvalPower() {
        Object result = executor.eval("2 ** 3");
        assertEquals(8, result);
    }

    // ==================== String Method Tests ====================

    @Test
    public void testEvalStringConcat() {
        Object result = executor.eval("'Hello' + ' ' + 'World'");
        assertEquals("Hello World", result);
    }

    @Test
    public void testEvalStringToUpperCase() {
        Object result = executor.eval("'hello'.toUpperCase()");
        assertEquals("HELLO", result);
    }

    @Test
    public void testEvalStringToLowerCase() {
        Object result = executor.eval("'HELLO'.toLowerCase()");
        assertEquals("hello", result);
    }

    @Test
    public void testEvalStringLength() {
        Object result = executor.eval("'Hello'.length()");
        assertEquals(5, result);
    }

    @Test
    public void testEvalStringSubstring() {
        Object result = executor.eval("'Hello World'.substring(6)");
        assertEquals("World", result);
    }

    @Test
    public void testEvalStringContains() {
        Object result = executor.eval("'Hello World'.contains('World')");
        assertEquals(true, result);
    }

    @Test
    public void testEvalStringReplace() {
        Object result = executor.eval("'Hello World'.replace('World', 'Groovy')");
        assertEquals("Hello Groovy", result);
    }

    @Test
    public void testEvalStringSplit() {
        Object result = executor.eval("'a,b,c'.split(',')");
        assertArrayEquals(new String[]{"a", "b", "c"}, (String[]) result);
    }

    // ==================== Binding Variable Tests ====================

    @Test
    public void testSetAndGetVariable() {
        executor.setVariable("x", 10);
        Object result = executor.eval("x + 5");
        assertEquals(15, result);
    }

    @Test
    public void testMultipleVariables() {
        executor.setVariable("a", 3);
        executor.setVariable("b", 4);
        Object result = executor.eval("a * b");
        assertEquals(12, result);
    }

    @Test
    public void testVariableInString() {
        executor.setVariable("name", "Groovy");
        Object result = executor.eval("'Hello ' + name");
        assertEquals("Hello Groovy", result);
    }

    @Test
    public void testSetVariablesMap() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", 10);
        variables.put("y", 20);
        executor.setVariables(variables);
        Object result = executor.eval("x + y");
        assertEquals(30, result);
    }

    @Test
    public void testGetVariables() {
        executor.setVariable("a", 1);
        executor.setVariable("b", 2);
        Map<String, Object> variables = executor.getVariables();
        assertEquals(1, variables.get("a"));
        assertEquals(2, variables.get("b"));
    }

    @Test
    public void testEvalWithVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 5);
        variables.put("b", 3);
        Object result = executor.eval("a + b", variables);
        assertEquals(8, result);
    }

    @Test
    public void testVariableIsolation() {
        executor.setVariable("x", 100);
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", 200);
        executor.eval("x", variables);
        assertEquals(100, executor.getVariable("x"));
    }

    // ==================== Exception Tests ====================

    @Test(expected = GroovyException.class)
    public void testEvalInvalidSyntax() {
        executor.eval("1 +");
    }

    @Test(expected = GroovyException.class)
    public void testEvalUnclosedParenthesis() {
        executor.eval("(1 + 2");
    }

    @Test(expected = GroovyException.class)
    public void testEvalUndefinedVariable() {
        executor.eval("undefinedVariable + 1");
    }

    @Test(expected = GroovyException.class)
    public void testEvalInvalidMethodCall() {
        executor.eval("'string'.nonExistentMethod()");
    }

    @Test
    public void testGroovyExceptionContainsMessage() {
        try {
            executor.eval("1 +");
            fail("Expected GroovyException");
        } catch (GroovyException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Failed to evaluate"));
        }
    }

    @Test
    public void testGroovyExceptionContainsCause() {
        try {
            executor.eval("1 +");
            fail("Expected GroovyException");
        } catch (GroovyException e) {
            assertNotNull(e.getCause());
        }
    }

    // ==================== Edge Cases ====================

    @Test
    public void testEvalEmptyString() {
        Object result = executor.eval("''");
        assertEquals("", result);
    }

    @Test(expected = GroovyException.class)
    public void testEvalZeroDivision() {
        executor.eval("1 / 0");
    }

    @Test
    public void testEvalNullVariable() {
        executor.setVariable("nullVar", null);
        Object result = executor.eval("nullVar");
        assertNull(result);
    }

    @Test
    public void testEvalBoolean() {
        Object result = executor.eval("true && false");
        assertEquals(false, result);
    }

    @Test
    public void testEvalList() {
        Object result = executor.eval("[1, 2, 3, 4, 5]");
        assertNotNull(result);
    }

    @Test
    public void testEvalListAccess() {
        Object result = executor.eval("[1, 2, 3][0]");
        assertEquals(1, result);
    }

    @Test
    public void testClearVariables() {
        executor.setVariable("x", 10);
        assertEquals(10, executor.getVariable("x"));
        executor.clearVariables();
        // After clear, variables should be set to null, not removed
        assertNull(executor.getVariable("x"));
    }
}
