package com.zifang.util.core.schedule.listener;

import com.zifang.util.core.schedule.JobDetail;
import com.zifang.util.core.schedule.JobKey;
import com.zifang.util.core.schedule.Trigger;
import com.zifang.util.core.schedule.TriggerKey;

/**
 * 自研调度器监听器接口。
 */
public interface SchedulerListener {
    default void jobAdded(JobDetail jobDetail) {}
    default void jobDeleted(JobKey jobKey) {}
    default void jobScheduled(Trigger trigger) {}
    default void jobUnscheduled(TriggerKey triggerKey) {}
    default void triggerPaused(TriggerKey triggerKey) {}
    default void triggersPaused(String triggerGroup) {}
    default void triggerResumed(TriggerKey triggerKey) {}
    default void triggersResumed(String triggerGroup) {}
    default void jobPaused(JobKey jobKey) {}
    default void jobsPaused(String jobGroup) {}
    default void jobResumed(JobKey jobKey) {}
    default void jobsResumed(String jobGroup) {}
    default void schedulerError(String msg, Throwable cause) {}
    default void schedulerShutdown() {}
    default void triggerFinalized(Trigger trigger) {}
    default void schedulingDataCleared() {}
    default void schedulerStarting() {}
    default void schedulerStarted() {}
    default void schedulerInStandbyMode() {}
    default void schedulerShuttingdown() {}
}
