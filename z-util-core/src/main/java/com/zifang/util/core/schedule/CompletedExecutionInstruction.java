package com.zifang.util.core.schedule;

/**
 * 任务执行结果指令。对标 {@code org.quartz.JobExecutionContext#setResult}。
 */
public enum CompletedExecutionInstruction {
    NOOP,
    RE_EXECUTE_JOB,
    SET_TRIGGER_COMPLETE,
    DELETE_TRIGGER,
    SET_ALL_TRIGGERS_COMPLETE,
    SET_TRIGGER_ERROR,
    SET_UNRECOVERABLE_ERROR
}
