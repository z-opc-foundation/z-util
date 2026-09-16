package com.zifang.util.core.schedule;

/**
 * 自研适配器：把 {@link Runnable} 适配为 {@link Job}。
 */
public class RunnableJobAdapter implements Job {
    private final Runnable delegate;
    public RunnableJobAdapter() { this.delegate = null; }
    public RunnableJobAdapter(Runnable delegate) { this.delegate = delegate; }
    @Override
    public void execute(JobExecutionContext context) {
        if (delegate != null) delegate.run();
        else {
            Object r = context.getMergedJobDataMap().get("runnable");
            if (r instanceof Runnable) ((Runnable) r).run();
        }
    }
}
