package com.zifang.util.expression.instruction;

import java.util.List;

/**
 * 指令执行器
 * 负责执行指令列表，实现表达式的求值运算
 *
 * @author zifang
 * @version 1.0
 */
public class InstructionExecutor {

    /**
     * 执行指令列表
     *
     * @param stack       操作数栈，用于存储中间计算结果
     * @param commandList 指令列表，包含要执行的指令序列
     * @return 执行结果，通常是表达式的求值结果
     */
    public Object execute(OperatorStack stack, List<Instruction> commandList) {
        return null;
    }

    /**
     * 加载指令集
     * 初始化指令与处理方法的映射关系
     */
    public void loadInstructionSet() {

    }
}
