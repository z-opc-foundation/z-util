package com.zifang.util.zex.bust.charpter12;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 解决原子性问题，保留可见性
 *
 * @author zifang
 */
public class TestSolveAtomic {

    private static long valueOffset;
    private static Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            valueOffset = unsafe.objectFieldOffset(TestSolveAtomic.class.getDeclaredField("count"));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public long count = 0;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        TestSolveAtomic test = new TestSolveAtomic();
        Thread th1 = new Thread(() -> {
            test.add10K();
        });
        Thread th2 = new Thread(() -> {
            test.add10K();
        });
        // 启动两个线程
        th1.start();
        th2.start();
        // 等待两个线程执行结束
        th1.join();
        th2.join();
        System.out.println(test.count);
    }

    private void add10K() {
        int idx = 0;
        while (idx++ < 10000) {
            while (!unsafe.compareAndSwapLong(this, valueOffset, count, count + 1)) {
                continue;
            }
            //count += 1;
        }
    }
}
