package com.zifang.util.core.concurrency.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zifang Lee (oldratlee at gmail dot com)
 */

/**
 * SynchronizationOnMutableFieldDemo类。
 */
public class SynchronizationOnMutableFieldDemo {
    static final int ADD_COUNT = 10000;
    private volatile List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        SynchronizationOnMutableFieldDemo demo = new SynchronizationOnMutableFieldDemo();

        Thread thread1 = new Thread(demo.getConcurrencyCheckTask());
        thread1.start();
        Thread thread2 = new Thread(demo.getConcurrencyCheckTask());
        thread2.start();

        thread1.join();
        thread2.join();

        int actualSize = demo.listeners.size();
        int expectedSize = ADD_COUNT * 2;
        if (actualSize != expectedSize) {
            // 在我的开发机上，几乎必现！（简单安全的解法：final List字段并用并发安全的List，如CopyOnWriteArrayList）
            System.err.printf("Fuck! Lost update on mutable field! actual %s expected %s.\n", actualSize, expectedSize);
        } else {
            System.out.println("Emm... Got right answer!!");
        }
    }

    /**
     * addListener方法。
     * * @param listener Listener类型参数
     */
    public void addListener(Listener listener) {
        synchronized (listeners) {
            List<Listener> results = new ArrayList<Listener>(listeners);
            results.add(listener);
            listeners = results;
        }
    }

    ConcurrencyCheckTask getConcurrencyCheckTask() {
        return new ConcurrencyCheckTask();
    }

    static class Listener {
        // stub class
    }

    private class ConcurrencyCheckTask implements Runnable {
        @Override
        /**
         * run方法。
         */
        public void run() {
            System.out.println("ConcurrencyCheckTask started!");
            for (int i = 0; i < ADD_COUNT; ++i) {
                addListener(new Listener());
            }
            System.out.println("ConcurrencyCheckTask stopped!");
        }
    }
}
