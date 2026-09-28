package com.zifang.util.core.lang.concurrency;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 提交即传播 MDC 日志上下文的线程池，构造参数与 JDK {@link ThreadPoolExecutor} 一致。
 * <p>
 * 传播语义见 {@link MdcPropagation}。JDK 的 {@code submit} 系列最终都走 {@code execute}，
 * 因此只覆盖 {@code execute} 即可同时管住 {@code execute/submit/invoke} 三条入口。
 * 池本身仍是标准线程池，可被 z-util-monitor 的线程池状态采集直接读取。
 */
public class MdcThreadPoolExecutor extends ThreadPoolExecutor {

    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                 TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                 TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                 TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                 RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public MdcThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                                 TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                 ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(MdcPropagation.wrap(command));
    }
}
