package com.zifang.util.expression.instruction;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 指令定义类
 * 通过注解方式定义所有可用的指令，并提供指令注册初始化功能
 *
 * @author zifang
 * @version 1.0
 */
public class InstructionDefine {

    /**
     * 指令码到处理方法的映射表
     */
    public static Map<String, Method> instructionMap = new HashMap<>();

    /**
     * 示例指令：idda
     */
    @CommandAnnotation("idda")
    public static void idda() {

    }

    /**
     * 初始化指令注册表
     * 扫描所有带有{@link CommandAnnotation}注解的方法，并注册到指令映射表中
     */
    public static void init() {
        for (Method method : InstructionDefine.class.getMethods()) {
            String instructionCode = method.getAnnotation(CommandAnnotation.class).value();
            instructionMap.put(instructionCode, method);
        }
    }
}
