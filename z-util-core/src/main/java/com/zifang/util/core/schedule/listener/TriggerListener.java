package com.zifang.util.core.schedule.listener;

import com.zifang.util.core.schedule.CompletedExecutionInstruction;
import com.zifang.util.core.schedule.JobExecutionContext;
import com.zifang.util.core.schedule.Trigger;

/**
 * 自研触发器监听器接口。
 */
public interface TriggerListener {
    String getName();
    default void triggerFired(Trigger trigger, JobExecutionContext context) {}
    default boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) { return false; }
    default void triggerMisfired(Trigger trigger) {}
    default void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction instruction) {}
}
