package com.zifang.util.core.resilience;

import java.util.concurrent.*;

/**
 * 超时控制器（自研，对标 Resilience4j TimeLimiter）。
 * <p>
 * 原理：把任务提交到独立线程池，主线程 {@code future.get(timeout)}；超时则取消并抛异常。
 * <p>
 * 注意：中断（interrupt）只对响应中断的代码有效；如果任务在 IO 阻塞无法中断，cancel 不能强制停。
 * 真要强制停得用 {@code sun.misc.Signal} 或 native。日常够用。
 */
public class TimeLimiter {

    private final ExecutorService executor;
    private final long defaultTimeoutMs;

    public TimeLimiter(int parallelism, long defaultTimeoutMs) {
        this.defaultTimeoutMs = defaultTimeoutMs;
        this.executor = Executors.newFixedThreadPool(parallelism, r -> {
            Thread t = new Thread(r, "z-util-timelimiter");
            t.setDaemon(true);
            return t;
        });
    }

    public <T> T call(Callable<T> task, long timeoutMs) {
        Future<T> f = executor.submit(task);
        try {
            return f.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            f.cancel(true);
            throw new ResilienceException("task timeout after " + timeoutMs + "ms");
        } catch (InterruptedException e) {
            f.cancel(true);
            Thread.currentThread().interrupt();
            throw new ResilienceException("interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new ResilienceException("task failed", cause);
        }
    }

    public <T> T call(Callable<T> task) {
        return call(task, defaultTimeoutMs);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
