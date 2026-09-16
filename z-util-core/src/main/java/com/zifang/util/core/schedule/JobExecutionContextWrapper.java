package com.zifang.util.core.schedule;

/**
 * 兼容旧 API 的上下文包装。
 */
public class JobExecutionContextWrapper {
    private final JobExecutionContext delegate;
    public JobExecutionContextWrapper(JobExecutionContext delegate) { this.delegate = delegate; }
    public JobExecutionContext getDelegate() { return delegate; }
}
