package com.zifang.util.zex;

/**
 * 工具类。
 * <p>
 * 此类提供常用的工具方法。
 *
 * @author zifang
 * @version 1.0
 */

import org.openjdk.jol.info.ClassLayout;

/**
 * ClassLayoutTest类。
 */
public class ClassLayoutTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ClassLayoutTest example = new ClassLayoutTest();
        //使用JOL工具打印对象的内存布局
        System.out.println(ClassLayout.parseInstance(example).toPrintable());
    }
}