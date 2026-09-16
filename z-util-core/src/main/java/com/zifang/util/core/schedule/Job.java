package com.zifang.util.core.schedule;

/**
 * 自研任务接口。对标 {@code org.quartz.Job}。
 * <p>
 * 业务方实现 {@link #execute(JobExecutionContext)} 即可被 {@link SchedulerManager} 调度。
 */
public interface Job {

    void execute(JobExecutionContext context);
}
