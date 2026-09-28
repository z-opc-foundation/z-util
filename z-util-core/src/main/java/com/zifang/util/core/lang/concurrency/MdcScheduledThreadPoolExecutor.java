package com.zifang.util.core.lang.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 提交即传播 MDC 日志上下文的定时线程池。
 * <p>
 * JDK 的 {@code ScheduledThreadPoolExecutor} 内部把任务包成 {@code ScheduledFutureTask}
 * 后再走 {@code execute}，所以只覆盖 {@code execute} 不够——延迟/周期任务在
 * {@code schedule} 入口就先被包装了。这里显式覆盖四个 schedule 入口。
 * <p>
 * 周期任务每次触发都复用<b>首次提交</b>时的上下文快照（周期任务无法预知"下一次由谁提交"），
 * 因此长时间运行的定时任务如需新鲜 traceId，应在任务体内部自己
 * {@link com.zifang.util.core.trace.TraceContextHolder#startNew()}。
 */
public class MdcScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    public MdcScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public MdcScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(MdcPropagation.wrap(command));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return super.schedule(MdcPropagation.wrap(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return super.schedule(MdcPropagation.wrap(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,
                                                  long period, TimeUnit unit) {
        return super.scheduleAtFixedRate(MdcPropagation.wrap(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
                                                     long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(MdcPropagation.wrap(command), initialDelay, delay, unit);
    }
}
