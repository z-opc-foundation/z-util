package com.zifang.util.core.concurrency.problems.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zifang
 */

/**
 * Utils类。
 */
public class Utils {
    static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    static final int THREAD_COUNT = CPU_COUNT * 2;
    static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    static volatile boolean isLoadMade = false;

    /**
     * makeLoad方法。
     *
     * @return static synchronized void类型返回值
     */
    public static synchronized void makeLoad() {
        if (isLoadMade) return;

        for (int i = 0; i < THREAD_COUNT; ++i) {
            executorService.submit(new Runnable() {
                @Override
                /**
                 * run方法。
                 */
                public void run() {
                    for (int i = 1; ; ++i) {
                        if (i % 1000000 == 0) {
                            sleep(1);
                        }
                    }
                }
            });
        }
    }

    /**
     * sleep方法。
     * * @param l long类型参数
     *
     * @return static void类型返回值
     */
    public static void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
        }
    }
}
