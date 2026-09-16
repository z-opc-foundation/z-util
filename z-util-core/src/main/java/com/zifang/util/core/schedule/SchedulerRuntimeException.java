package com.zifang.util.core.schedule;

/**
 * 调度器运行期异常。
 */
public class SchedulerRuntimeException extends RuntimeException {
    public SchedulerRuntimeException(String message) { super(message); }
    public SchedulerRuntimeException(String message, Throwable cause) { super(message, cause); }
}
