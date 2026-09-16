package com.zifang.util.zex.bust.charpter12;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
public class FalseSharingExample implements Runnable {

    public final static long ITERATIONS = 500L * 1000L * 100L;
    private static ValueNoPadding[] longs;
    private int arrayIndex = 0;

    /**
     * FalseSharingExample方法。
     * * @param arrayIndex final类型参数
     */
    public FalseSharingExample(final int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    private static void runTest(int NUM_THREADS) throws InterruptedException {
        Thread[] threads = new Thread[NUM_THREADS];
        longs = new ValueNoPadding[NUM_THREADS];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = new ValueNoPadding();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new FalseSharingExample(i));
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    /**
     * main方法。
     * * @param args final类型参数
     *
     * @return static void类型返回值
     */
    public static void main(final String[] args) throws Exception {
        for (int i = 1; i < 10; i++) {
            System.gc();
            final long start = System.currentTimeMillis();
            runTest(i);
            System.out.println(i + " Threads, duration = " + (System.currentTimeMillis() - start));
        }
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        long i = ITERATIONS + 1;
        while (0 != --i) {
            longs[arrayIndex].value = 0L;
        }
    }
}

class ValuePadding {
    protected long p1, p2, p3, p4, p5, p6, p7; //前置填充
    protected volatile long value = 0L;
    protected long p9, p10, p11, p12, p13, p14, p15; //后置填充
}

class ValueNoPadding {
    protected volatile long value = 0L;
}