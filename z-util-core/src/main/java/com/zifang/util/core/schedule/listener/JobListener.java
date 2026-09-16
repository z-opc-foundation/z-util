package com.zifang.util.core.schedule.listener;

import com.zifang.util.core.schedule.JobExecutionContext;

/**
 * 自研任务监听器接口。
 */
public interface JobListener {
    String getName();
    default void jobToBeExecuted(JobExecutionContext context) {}
    default void jobExecutionVetoed(JobExecutionContext context) {}
    default void jobWasExecuted(JobExecutionContext context, Throwable exception) {}
}
