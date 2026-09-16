package com.zifang.util.zex.bust.chapter9;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.lang.annotation.Annotation;
import java.util.Arrays;

@Demo("类注解")
/**
 * ElementTest类。
 */
public class ElementTest {

    @Demo("字段注解")
    private String name;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Class<?> clazz = ElementTest.class;
        //根据指定注解类型获取该注解
        Demo demo = clazz.getAnnotation(Demo.class);
        System.out.println("A:" + demo);

        //获取该元素上的所有注解，包含从父类继承
        Annotation[] an = clazz.getAnnotations();
        System.out.println("an:" + Arrays.toString(an));
        //获取该元素上的所有注解，但不包含继承
        Annotation[] an2 = clazz.getDeclaredAnnotations();
        System.out.println("an2:" + Arrays.toString(an2));

        //判断注解Demo是否在该元素上
        boolean b = clazz.isAnnotationPresent(Demo.class);
        System.out.println("b:" + b);
    }

    @Demo("方法注解")
    /**
     * f方法。
     *      * @param @Demo("参数a" Object类型参数
     */
    public void f(@Demo("参数a") String a, @Demo("参数b") String b) {

    }
}
