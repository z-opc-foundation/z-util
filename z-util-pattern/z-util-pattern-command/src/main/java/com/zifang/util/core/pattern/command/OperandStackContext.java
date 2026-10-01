package com.zifang.util.core.pattern.command;

import java.util.Map;

/**
 * 带操作数栈的命令上下文
 *
 * @author zifang
 */
public class OperandStackContext extends CommandContext {

    private final OperandStack operandStack;

    /**
     * OperandStackContext方法。
     */
    public OperandStackContext() {
        this.operandStack = new OperandStack();
    }

    /**
     * OperandStackContext方法。
     * * @param initialData MapString,类型参数
     */
    public OperandStackContext(Map<String, Object> initialData) {
        super(initialData);
        this.operandStack = new OperandStack();
    }

    /**
     * OperandStackContext方法。
     * * @param stackSize int类型参数
     */
    public OperandStackContext(int stackSize) {
        this.operandStack = new OperandStack(stackSize);
    }

    /**
     * OperandStackContext方法。
     * * @param initialData MapString,类型参数
     *
     * @param stackSize int类型参数
     */
    public OperandStackContext(Map<String, Object> initialData, int stackSize) {
        super(initialData);
        this.operandStack = new OperandStack(stackSize);
    }

    /**
     * 获取操作数栈
     */
    public OperandStack getOperandStack() {
        return operandStack;
    }

    /**
     * 快捷方法：入栈
     */
    public void push(Object value) {
        operandStack.push(value);
    }

    /**
     * 快捷方法：出栈
     */
    @SuppressWarnings("unchecked")
    /**
     * pop方法。
     * @return <T> T类型返回值
     */
    public <T> T pop() {
        return (T) operandStack.pop();
    }

    /**
     * 快捷方法：查看栈顶
     */
    @SuppressWarnings("unchecked")
    /**
     * peek方法。
     * @return <T> T类型返回值
     */
    public <T> T peek() {
        return (T) operandStack.peek();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "OperandStackContext{" +
                "data=" + keySet() +
                ", stack=" + operandStack +
                ", executedCommands=" + getExecutedCommands().size() +
                ", interrupted=" + isInterrupted() +
                '}';
    }
}
