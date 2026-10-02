package com.zifang.util.expression.instruction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Operator 运算符枚举完整测试
 */
public class OperatorTest {

    @Test
    public void testOperatorValues() {
        Operator[] operators = Operator.values();
        assertEquals(4, operators.length);
    }

    @Test
    public void testAddOperator() {
        Operator add = Operator.ADD;
        assertEquals("加法", add.getName());
        assertEquals("+", add.getValue());
        assertEquals("ADD", add.name());
    }

    @Test
    public void testSubtractOperator() {
        Operator sub = Operator.SUBTRACT;
        assertEquals("减法", sub.getName());
        assertEquals("-", sub.getValue());
    }

    @Test
    public void testMultiplyOperator() {
        Operator mul = Operator.MULTIPLY;
        assertEquals("乘法", mul.getName());
        assertEquals("*", mul.getValue());
    }

    @Test
    public void testDivideOperator() {
        Operator div = Operator.DIVIDE;
        assertEquals("除法", div.getName());
        assertEquals("/", div.getValue());
    }

    @Test
    public void testValueOf() {
        assertEquals(Operator.ADD, Operator.valueOf("ADD"));
        assertEquals(Operator.SUBTRACT, Operator.valueOf("SUBTRACT"));
        assertEquals(Operator.MULTIPLY, Operator.valueOf("MULTIPLY"));
        assertEquals(Operator.DIVIDE, Operator.valueOf("DIVIDE"));
    }

    @Test
    public void testOperatorEquality() {
        assertEquals(Operator.ADD, Operator.ADD);
        assertNotEquals(Operator.ADD, Operator.SUBTRACT);
    }
}
