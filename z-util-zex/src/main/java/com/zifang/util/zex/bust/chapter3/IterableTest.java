package com.zifang.util.zex.bust.chapter3;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * IterableTest类。
 */
public class IterableTest {

    @Test
    /**
     * test001方法。
     */
    public void test001() {
        Collection<String> c = Arrays.asList("1", "2", "3");
        Iterator<String> iterator = c.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() {
        Collection<String> c = Arrays.asList("1", "2", "3");
        for (String s : c) {
            System.out.println(s);
        }
    }
}
