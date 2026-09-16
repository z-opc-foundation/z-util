package com.zifang.util.zex.bust.chapter10;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

/**
 * ParserClassTest001类。
 */
public class ParserClassTest001 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        // 获得类基础信息
        Class<?> c = ParserClassTest001.class;

        System.out.println("类包名:" + c.getPackage().getName());
        System.out.println("类名:" + c.getName());
        System.out.println("类短名:" + c.getSimpleName());


        System.out.println("类是否是接口类型:" + c.isInterface());
        System.out.println("类是否是基本变量类型:" + c.isPrimitive());
        System.out.println("类是否是数组类型:" + c.isArray());


    }
}
