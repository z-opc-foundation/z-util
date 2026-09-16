package com.zifang.util.expression.instruction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * OperatorStack 操作数栈完整测试
 */
public class OperatorStackTest {

    @Test
    public void testPushAndPop() {
        OperatorStack stack = new OperatorStack();
        stack.push(Integer.valueOf(10));
        stack.push(Integer.valueOf(20));
        assertEquals(Integer.valueOf(20), stack.pop());
        assertEquals(Integer.valueOf(10), stack.pop());
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testPopEmpty() {
        OperatorStack stack = new OperatorStack();
        stack.pop();
    }

    @Test
    public void testPushPopSingle() {
        OperatorStack stack = new OperatorStack();
        stack.push("hello");
        assertEquals("hello", stack.pop());
    }

    @Test
    public void testLifoOrder() {
        OperatorStack stack = new OperatorStack();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
    }

    @Test
    public void testMixedTypes() {
        OperatorStack stack = new OperatorStack();
        stack.push(100);
        stack.push("string");
        stack.push(3.14);

        assertEquals(3.14, stack.pop());
        assertEquals("string", stack.pop());
        assertEquals(100, stack.pop());
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testEmptyAfterPopAll() {
        OperatorStack stack = new OperatorStack();
        stack.push(1);
        stack.pop();
        stack.pop(); // 第二次pop空栈，抛异常
    }
}
