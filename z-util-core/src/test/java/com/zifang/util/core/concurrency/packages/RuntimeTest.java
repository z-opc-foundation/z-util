package com.zifang.util.core.concurrency.packages;

/**
 * RuntimeTest类。
 */
public class RuntimeTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws InterruptedException {
        //获取运行的JVM虚拟机
        Runtime run = Runtime.getRuntime();
        //注册JVM虚拟机运行完后执行的事件
        run.addShutdownHook(new Thread(new Runnable() {

            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("over!");
            }
        }));
        Thread.sleep(10000);
    }
}
