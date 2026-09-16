package com.zifang.util.zex.bust.charpter12;

/**
 * 解决原子性问题，保留可见性
 */
public class TestSolveView {
    private static int count = 0;

    /**
     * calc方法。
     *
     * @return static long类型返回值
     */
    public static long calc() throws InterruptedException {
        TestSolveView test = new TestSolveView();
        // 创建两个线程，执行add()操作
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
        return count;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println(calc());
    }

    private void add10K() {
        int idx = 0;
        while (idx++ < 10000) {
            count++;
        }
    }
}
