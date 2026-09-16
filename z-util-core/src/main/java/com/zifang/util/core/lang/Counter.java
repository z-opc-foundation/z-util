package com.zifang.util.core.lang;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 简单计数器（对标 Codahale Metrics / Micrometer Counter）。
 * <p>
 * 线程安全的累加器，适合统计 QPS、调用次数等指标。
 *
 * @author zifang
 */
public class Counter {

    private final AtomicLong count = new AtomicLong();

    /**
     * 当前计数。
     */
    public long get() {
        return count.get();
    }

    /**
     * 加 1。
     */
    public long increment() {
        return increment(1L);
    }

    /**
     * 加 n（n 可以为负，表示减少）。
     */
    public long increment(long n) {
        return count.addAndGet(n);
    }

    /**
     * 减 1。
     */
    public long decrement() {
        return increment(-1L);
    }

    /**
     * 重置为 0，返回旧值。
     */
    public long reset() {
        return count.getAndSet(0L);
    }

    @Override
    public String toString() {
        return "Counter[" + count.get() + "]";
    }
}