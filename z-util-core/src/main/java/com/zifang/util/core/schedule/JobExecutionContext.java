package com.zifang.util.core.schedule;

/**
 * 任务执行上下文。对标 {@code org.quartz.JobExecutionContext}。
 */
public class JobExecutionContext {

    private final JobDetail jobDetail;
    private final Trigger trigger;
    private final JobDataMap dataMap;
    private final long fireTime;
    private final long scheduledFireTime;

    public JobExecutionContext(JobDetail jobDetail, Trigger trigger,
                               long fireTime, long scheduledFireTime) {
        this.jobDetail = jobDetail;
        this.trigger = trigger;
        this.dataMap = new JobDataMap(jobDetail.getJobDataMap());
        this.fireTime = fireTime;
        this.scheduledFireTime = scheduledFireTime;
    }

    public JobDetail getJobDetail() {
        return jobDetail;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public JobDataMap getMergedJobDataMap() {
        return dataMap;
    }

    public long getFireTime() {
        return fireTime;
    }

    public long getScheduledFireTime() {
        return scheduledFireTime;
    }
}
