package com.zifang.util.expression.instruction;

import java.util.LinkedList;

/**
 * 操作符栈
 * 用于表达式求值时的操作符管理，实现运算符优先级解析
 *
 * @author zifang
 * @version 1.0
 */
public class OperatorStack {

    /**
     * 内部使用链表实现的栈
     */
    private LinkedList<Object> stack = null;

    /**
     * 构造函数，初始化空栈
     */
    public OperatorStack() {
        stack = new LinkedList<>();
    }

    /**
     * 将元素压入栈顶
     *
     * @param o 要压入的元素
     */
    public void push(Object o) {
        stack.push(o);
    }

    /**
     * 弹出并返回栈顶元素
     *
     * @return 栈顶元素，如果栈为空则返回null
     */
    public Object pop() {
        return stack.pop();
    }

}
