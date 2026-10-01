package com.zifang.util.core.pattern.command;

import java.util.*;

/**
 * 操作数栈
 * <p>
 * 模拟JVM操作数栈的行为，支持push/pop/dup等操作
 *
 * @author zifang
 */
public class OperandStack {

    private final Deque<Object> stack;
    private final int maxSize;

    /**
     * OperandStack方法。
     */
    public OperandStack() {
        this(64);
    }

    /**
     * OperandStack方法。
     * * @param maxSize int类型参数
     */
    public OperandStack(int maxSize) {
        this.stack = new ArrayDeque<>(maxSize);
        this.maxSize = maxSize;
    }

    /**
     * 入栈
     */
    public void push(Object value) {
        if (stack.size() >= maxSize) {
            throw new StackOverflowError("Operand stack overflow");
        }
        stack.push(value);
    }

    /**
     * 出栈
     */
    @SuppressWarnings("unchecked")
    /**
     * pop方法。
     * @return <T> T类型返回值
     */
    public <T> T pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Operand stack is empty");
        }
        return (T) stack.pop();
    }

    /**
     * 查看栈顶元素
     */
    @SuppressWarnings("unchecked")
    /**
     * peek方法。
     * @return <T> T类型返回值
     */
    public <T> T peek() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Operand stack is empty");
        }
        return (T) stack.peek();
    }

    /**
     * 查看栈顶下的元素
     */
    @SuppressWarnings("unchecked")
    /**
     * peekUnder方法。
     * @return <T> T类型返回值
     */
    public <T> T peekUnder() {
        if (stack.size() < 2) {
            throw new IllegalStateException("Not enough elements in stack");
        }
        Iterator<Object> it = stack.iterator();
        it.next();
        return (T) it.next();
    }

    /**
     * 复制栈顶元素
     */
    public void dup() {
        Object top = peek();
        push(top);
    }

    /**
     * 复制栈顶元素并插入下方
     */
    public void dupUnder() {
        Object top = pop();
        Object under = pop();
        push(top);
        push(under);
        push(top);
    }

    /**
     * 交换栈顶两个元素
     */
    public void swap() {
        Object a = pop();
        Object b = pop();
        push(a);
        push(b);
    }

    /**
     * 栈大小
     */
    public int size() {
        return stack.size();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * 清空栈
     */
    public void clear() {
        stack.clear();
    }

    /**
     * 将栈转为列表
     */
    public List<Object> toList() {
        return new ArrayList<>(stack);
    }

    /**
     * 获取栈的副本
     */
    public List<Object> getStack() {
        return new ArrayList<>(stack);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "OperandStack" + toList();
    }
}
