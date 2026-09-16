package com.zifang.util.zex.source;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.stream.Collectors;

/**
 * 类信息打印工具类。
 * <p>
 * 此类用于打印指定类的所有声明方法信息，
 * 包括方法的返回类型、名称和参数类型列表。
 *
 * @author zifang
 * @version 1.0
 */
public class ClassPrinter {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Class c = AbstractQueuedSynchronizer.class;

        for (Method method : c.getDeclaredMethods()) {
            String str = "";
            str = str + method.getReturnType().getName() + " " + method.getName();
            str = str + "(" + String.join(", ", Arrays.stream(method.getParameterTypes()).map(e -> e.getName()).collect(Collectors.toList())) + ")";
            System.out.println(str);
        }
    }
}
