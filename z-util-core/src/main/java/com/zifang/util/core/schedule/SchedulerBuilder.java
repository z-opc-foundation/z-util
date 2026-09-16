package com.zifang.util.core.schedule;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自研 SchedulerBuilder。构建 {@link SchedulerManager}。
 */
public final class SchedulerBuilder {

    private String name = "ZUtilScheduler";
    private int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors());
    private boolean daemonThreads = true;
    private String threadNamePrefix = "zutil-scheduler-";

    private SchedulerBuilder() {}

    public static SchedulerBuilder newScheduler() { return new SchedulerBuilder(); }

    public SchedulerBuilder withName(String name) { this.name = name; return this; }
    public SchedulerBuilder withThreadCount(int threadCount) {
        if (threadCount < 1) throw new IllegalArgumentException("threadCount must be >= 1");
        this.threadCount = threadCount; return this;
    }
    public SchedulerBuilder withDaemonThreads(boolean daemon) { this.daemonThreads = daemon; return this; }
    public SchedulerBuilder withThreadNamePrefix(String prefix) { this.threadNamePrefix = prefix; return this; }

    public SchedulerManager build() {
        Objects.requireNonNull(name, "name must not be null");
        ThreadFactory tf = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger(0);
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, threadNamePrefix + seq.incrementAndGet());
                t.setDaemon(daemonThreads);
                return t;
            }
        };
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(threadCount, tf);
        if (exec instanceof ScheduledThreadPoolExecutor) {
            ((ScheduledThreadPoolExecutor) exec).setRemoveOnCancelPolicy(true);
        }
        return new SchedulerManager(name, exec);
    }
}
