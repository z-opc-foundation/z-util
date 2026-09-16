package com.zifang.util.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * JvmTest类。
 */
public class JvmTest {

    @Test
    @org.junit.Ignore("JVM作业测试，故意触发OOM")
    /**
     * test0方法。
     */
    public void test0() {
        List<HeapOOM> list = new ArrayList<>();
        while (true) {
            list.add(new HeapOOM());
        }
    }

    @Test
    @org.junit.Ignore("JVM作业测试，故意触发StackOverflow")
    /**
     * test1方法。
     */
    public void test1() {
        StackOverflow stackOverflowError = new StackOverflow();
        try {
            stackOverflowError.stackLeak();
        } catch (Throwable e) {
            System.out.println("stack 深度:" + stackOverflowError.stackLength);
            throw e;
        }
    }

    static class HeapOOM {
        HeapOOM[] testlist = new HeapOOM[100000000];
    }

    static class StackOverflow {
        int stackLength = 1;

        /**
         * stackLeak方法。
         */
        public void stackLeak() {
            stackLength++;
            stackLeak();
        }
    }

}



