package com.zifang.util.zex.source;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer定时器测试类。
 * <p>
 * 此类演示了Java Timer定时器的基本用法。
 * Timer用于在后台线程中调度任务执行。
 *
 * @author zifang
 * @version 1.0
 */
public class TimerTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            /**
             * run方法。
             */
            public void run() {
                System.out.println("执行：" + System.currentTimeMillis());
            }
        }, 1000, 1000);
        System.out.println("开始执行：" + System.currentTimeMillis());
    }
}
