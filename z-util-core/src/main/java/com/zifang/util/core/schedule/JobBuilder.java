package com.zifang.util.core.schedule;

/**
 * 自研 JobBuilder 静态入口。
 * 对标 {@code org.quartz.JobBuilder}。
 */
public final class JobBuilder {

    private JobBuilder() {
    }

    public static JobDetail.Builder newJob(Class<? extends Job> jobClass) {
        return JobDetail.newBuilder().ofType(jobClass);
    }
}
