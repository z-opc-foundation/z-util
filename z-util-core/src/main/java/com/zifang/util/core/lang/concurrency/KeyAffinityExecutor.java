package com.zifang.util.core.lang.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 键亲和串行执行器。
 * <p>
 * 内部由 {@code stripeCount} 个"单线程池"组成，同一个 key 的任务恒定落到同一条 stripe，
 * 因此对同一个 key 天然串行、按提交顺序执行；不同 key 之间并行。
 * 适合"按业务主键串行化写"的场景（如同一条流程实例的状态变更、同一行的读改写）。
 *
 * <h3>与直接用锁的区别</h3>
 * 不会阻塞提交线程：每个 stripe 有自己的有界队列，队列满时按拒绝策略处理
 * （默认 {@link ThreadPoolExecutor#CallerRunsPolicy}，即在提交线程上执行，起背压作用）。
 *
 * <h3>线程模型</h3>
 * 每条 stripe 是核心线程数 1、最大线程数 1 的 {@link ThreadPoolExecutor}，
 * keepAlive 60s 且允许核心线程超时，因此空闲时会收缩到 0 条线程。
 */
public final class KeyAffinityExecutor {

    private static final int DEFAULT_QUEUE_CAPACITY = 4096;

    private final ThreadPoolExecutor[] stripes;
    private final String namePrefix;

    public KeyAffinityExecutor(int stripeCount) {
        this(stripeCount, "key-affinity", DEFAULT_QUEUE_CAPACITY);
    }

    public KeyAffinityExecutor(int stripeCount, String namePrefix, int queueCapacity) {
        if (stripeCount <= 0) {
            throw new IllegalArgumentException("stripeCount must be positive: " + stripeCount);
        }
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("queueCapacity must be positive: " + queueCapacity);
        }
        this.namePrefix = namePrefix == null ? "key-affinity" : namePrefix;
        this.stripes = new ThreadPoolExecutor[stripeCount];
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        for (int i = 0; i < stripeCount; i++) {
            BlockingQueue<Runnable> queue = queueCapacity == Integer.MAX_VALUE
                    ? new LinkedBlockingQueue<Runnable>()
                    : new ArrayBlockingQueue<Runnable>(queueCapacity);
            ThreadPoolExecutor stripe = new ThreadPoolExecutor(1, 1, 60L, TimeUnit.SECONDS, queue,
                    new StripeThreadFactory(this.namePrefix, i), handler);
            stripe.allowCoreThreadTimeOut(true);
            stripes[i] = stripe;
        }
    }

    /**
     * 提交一个任务到 key 对应的 stripe，不等待结果。
     */
    public void execute(Object key, Runnable task) {
        if (task == null) {
            return;
        }
        stripes[indexFor(key)].execute(task);
    }

    /**
     * 提交一个有返回值的任务到 key 对应的 stripe。
     */
    public <T> Future<T> submit(Object key, Callable<T> task) {
        return stripes[indexFor(key)].submit(task);
    }

    /**
     * 提交并等待结果，异常包装成 {@link IllegalStateException} 抛出。
     */
    public <T> T invoke(Object key, Callable<T> task) {
        try {
            return submit(key, task).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted awaiting key-affinity task", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("key-affinity task failed", e.getCause());
        }
    }

    /**
     * key 当前落在哪条 stripe（用于观测与测试）。
     */
    public int stripeOf(Object key) {
        return indexFor(key);
    }

    public int stripeCount() {
        return stripes.length;
    }

    /**
     * 所有 stripe 队列中尚未执行的任务总数。
     */
    public int pendingTasks() {
        int pending = 0;
        for (ThreadPoolExecutor stripe : stripes) {
            pending += stripe.getQueue().size();
        }
        return pending;
    }

    public void shutdown() {
        for (ThreadPoolExecutor stripe : stripes) {
            stripe.shutdown();
        }
    }

    public List<Runnable> shutdownNow() {
        List<Runnable> dropped = new ArrayList<>();
        for (ThreadPoolExecutor stripe : stripes) {
            dropped.addAll(stripe.shutdownNow());
        }
        return dropped;
    }

    public boolean isShutdown() {
        for (ThreadPoolExecutor stripe : stripes) {
            if (!stripe.isShutdown()) {
                return false;
            }
        }
        return true;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long deadlineNanos = System.nanoTime() + unit.toNanos(timeout);
        for (ThreadPoolExecutor stripe : stripes) {
            long remaining = deadlineNanos - System.nanoTime();
            if (remaining <= 0 || !stripe.awaitTermination(remaining, TimeUnit.NANOSECONDS)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 高 16 位混入低 16 位后取正模，避免 key 连号时全挤在同一条 stripe。
     */
    private int indexFor(Object key) {
        int hash = key == null ? 0 : key.hashCode();
        hash ^= (hash >>> 16);
        return (hash & 0x7fffffff) % stripes.length;
    }

    private static final class StripeThreadFactory implements ThreadFactory {

        private final String prefix;
        private final int stripeIndex;
        private final AtomicInteger seq = new AtomicInteger(1);

        private StripeThreadFactory(String prefix, int stripeIndex) {
            this.prefix = prefix;
            this.stripeIndex = stripeIndex;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, prefix + "-stripe" + stripeIndex + "-" + seq.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }
}
