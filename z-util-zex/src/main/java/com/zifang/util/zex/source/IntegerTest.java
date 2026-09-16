package com.zifang.util.zex.source;

import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Integer类测试类。
 * <p>
 * 此类用于测试Integer类的各种方法，包括parseInt、toHexString、toString等。
 *
 * @author zifang
 * @version 1.0
 */
public class IntegerTest {
    @Test
    /**
     * test001方法。
     */
    public void test001() {
        System.out.println();
        Integer.parseInt("-2147483649", 10);
    }


    @Test
    /**
     * test002方法。
     */
    public void test002() {
        System.out.println();
        Integer.toHexString(17);
    }

    @Test
    /**
     * test003方法。
     */
    public void test003() {
        System.out.println();
        Integer.toString(65536);

    }

    @Test
    /**
     * test004方法。
     */
    public void test004() {
        System.out.println();
    }

    @Test
    /**
     * ssss方法。
     */
    public void ssss() {
        for (Method method : Integer.class.getDeclaredMethods()) {
            System.out.println(method.getName() + "(" + method.getParameterTypes() + ")");
        }
    }


}
