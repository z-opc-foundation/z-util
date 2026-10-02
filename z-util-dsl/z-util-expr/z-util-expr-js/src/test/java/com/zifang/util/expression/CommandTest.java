package com.zifang.util.expression;

import com.zifang.util.expression.instruction.Instruction;
import com.zifang.util.expression.instruction.InstructionExecutor;
import com.zifang.util.expression.instruction.OperatorStack;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CommandTest {

    @Test
    public void test001() {

        OperatorStack stack = new OperatorStack();
        List<Instruction> commandList = new ArrayList<>();

        // 操作数栈 + 局部变量表
        InstructionExecutor instructionExecutor = new InstructionExecutor();
        instructionExecutor.loadInstructionSet();
//        instructionExecutor.
//        Object r = instructionExecutor.execute(stack, commandList);

    }

}
