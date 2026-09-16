package com.zifang.util.core.resilience;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 舱壁（Bulkhead，自研，对标 Resilience4j Bulkhead / semaphore-based isolation）。
 * <p>
 * 原理：用 {@link Semaphore} 限制并发数。超过最大并发 → 抛 {@link ResilienceException} 或等。
 * <p>
 * 两种模式：
 * <ul>
 *   <li><b>并发型</b>：限制同时在跑的任务数（最常用）</li>
 *   <li><b>队列型</b>：并发 + 等候队列；队列满则拒绝</li>
 * </ul>
 */
public class Bulkhead {

    private final Semaphore semaphore;
    private final int maxConcurrent;

    public Bulkhead(int maxConcurrent) {
        if (maxConcurrent <= 0) throw new IllegalArgumentException("maxConcurrent must be > 0");
        this.maxConcurrent = maxConcurrent;
        this.semaphore = new Semaphore(maxConcurrent);
    }

    public <T> T call(Callable<T> task) {
        if (!semaphore.tryAcquire()) {
            throw new ResilienceException("bulkhead full, maxConcurrent=" + maxConcurrent);
        }
        try {
            return task.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    /**
     * 等待型 acquire（用于定时任务等可以阻塞的场景）。
     */
    public <T> T callBlocking(Callable<T> task, long waitMs) throws InterruptedException {
        if (!semaphore.tryAcquire(waitMs, TimeUnit.MILLISECONDS)) {
            throw new ResilienceException("bulkhead timeout, maxConcurrent=" + maxConcurrent);
        }
        try {
            return task.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public int availablePermits() {
        return semaphore.availablePermits();
    }

    public int maxConcurrent() {
        return maxConcurrent;
    }
}
