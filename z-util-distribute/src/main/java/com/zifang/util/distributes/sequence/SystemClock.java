package com.zifang.util.distributes.sequence;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化方案。
 * <p>
 * System.currentTimeMillis()的调用比new出一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说约100倍左右）
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道
 * 后台定时更新时钟，JVM退出时，线程自动回收
 * <p>
 * 性能提升参考数据：
 * 10亿次调用：优化前43410ms，优化后206ms，提升210.7倍
 * 1亿次调用：优化前4699ms，优化后29ms，提升162倍
 * 1000万次：优化前480ms，优化后12ms，提升40倍
 * 100万次：优化前50ms，优化后10ms，提升5倍
 *
 * @author zifang
 */
public class SystemClock {

    private final long period;
    private final AtomicLong now;

    /**
     * 私有构造函数，禁止外部实例化
     *
     * @param period 更新周期（毫秒）
     */
    private SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    /**
     * 获取单例实例
     *
     * @return SystemClock单例实例
     */
    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 获取当前时间戳（毫秒）
     * <p>
     * 这是获取当前时间的主要方法，比System.currentTimeMillis()性能更好
     *
     * @return 当前时间戳（毫秒）
     */
    public static long now() {
        return instance().currentTimeMillis();
    }

    /**
     * 获取当前时间的日期字符串格式
     *
     * @return 当前时间戳对应的日期字符串，格式为yyyy-mm-dd hh:mm:ss.fffffffff
     */
    public static String nowDate() {
        return new Timestamp(instance().currentTimeMillis()).toString();
    }

    /**
     * 启动定时任务定期更新时钟
     */
    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            /**
             * newThread方法。
             *      * @param runnable Runnable类型参数
             * @return Thread类型返回值
             */
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable, "System Clock");
                thread.setDaemon(true);
                return thread;
            }
        });
        scheduler.scheduleAtFixedRate(new Runnable() {
            /**
             * run方法。
             */
            public void run() {
                now.set(System.currentTimeMillis());
            }
        }, period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取当前时间毫秒数（从缓存中获取）
     *
     * @return 当前时间戳（毫秒）
     */
    private long currentTimeMillis() {
        return now.get();
    }

    /**
     * 内部单例持有类
     */
    private static class InstanceHolder {
        /**
         * SystemClock方法。
         * * @param 1 Object类型参数
         *
         * @return static final SystemClock INSTANCE = new类型返回值
         */
        public static final SystemClock INSTANCE = new SystemClock(1);
    }

}
