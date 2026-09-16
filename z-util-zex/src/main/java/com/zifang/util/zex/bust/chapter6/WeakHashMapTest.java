package com.zifang.util.zex.bust.chapter6;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.WeakHashMap;

/**
 * WeakHashMapTest类。
 */
public class WeakHashMapTest {

    private static final Logger logger = LoggerFactory.getLogger(WeakHashMapTest.class);

    /**
     * WeakHashMapTest方法。
     */
    public WeakHashMapTest() {

    }

    @Test
    /**
     * test001方法。
     */
    public void test001() throws InterruptedException {

        WeakHashMap<WeakHashMapTest2, String> w = new WeakHashMap<>();
        w.put(new WeakHashMapTest2("1"), "1");
        w.put(new WeakHashMapTest2("2"), "2");
        w.put(new WeakHashMapTest2("3"), "3");
        byte[] bArray = new byte[1024 * 1024 * 1024];
        byte[] bArra1 = new byte[1024 * 1024 * 1024];
        System.out.println("WeakHashMap 的内容：" + w);

        System.gc();

        Thread.sleep(10000);
        System.out.println("WeakHashMap 的内容：" + w);

        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        byte[] bArra14 = new byte[1024 * 1024 * 400];
        System.gc();


    }

    class WeakHashMapTest2 {
        private String a;

        /**
         * WeakHashMapTest2方法。
         * * @param a String类型参数
         */
        public WeakHashMapTest2(String a) {
            this.a = a;
        }

    }
}
