package com.zifang.util.proxy.jol;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.util.HashMap;

/**
 * https://github.com/ddean2009/learn-java-base-9-to-20
 */

/**
 * test类。
 */
public class test {

    //public static final log log = logFactory.getlog(test.class);

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        HashMap hashMap = new HashMap();
        hashMap.put("flydean", "www.flydean.com");


        System.out.println(VM.current().details());
        System.out.println(ClassLayout.parseClass(String.class).toPrintable());
        System.out.println(ClassLayout.parseInstance("www.flydean.com").toPrintable());
        System.out.println(ClassLayout.parseClass(byte[].class).toPrintable());
        System.out.println(ClassLayout.parseInstance("www.flydean.com".getBytes()).toPrintable());
        System.out.println(ClassLayout.parseClass(Long.class).toPrintable());
        System.out.println(ClassLayout.parseInstance(1234567890111112L).toPrintable());
        System.out.println(GraphLayout.parseInstance(hashMap).toPrintable());
    }
}
