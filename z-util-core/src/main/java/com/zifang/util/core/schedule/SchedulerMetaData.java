package com.zifang.util.core.schedule;

/**
 * 自研调度器元数据。简化版，对标 {@code org.quartz.SchedulerMetaData}。
 */
public class SchedulerMetaData {

    private final String schedulerName;
    private final boolean started;
    private final int jobStoreSize;
    private final int threadPoolSize;

    public SchedulerMetaData(String schedulerName, boolean started,
                             int jobStoreSize, int threadPoolSize) {
        this.schedulerName = schedulerName;
        this.started = started;
        this.jobStoreSize = jobStoreSize;
        this.threadPoolSize = threadPoolSize;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public boolean isStarted() {
        return started;
    }

    public int getNumberOfJobs() {
        return jobStoreSize;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
