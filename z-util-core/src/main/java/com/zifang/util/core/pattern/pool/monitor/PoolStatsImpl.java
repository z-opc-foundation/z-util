package com.zifang.util.core.pattern.pool.monitor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 池统计数据实现
 */
public class PoolStatsImpl implements PoolStats {

    private final AtomicLong borrowCount = new AtomicLong(0);
    private final AtomicLong returnCount = new AtomicLong(0);
    private final AtomicLong createCount = new AtomicLong(0);
    private final AtomicLong destroyCount = new AtomicLong(0);
    private final AtomicLong validateCount = new AtomicLong(0);

    private final AtomicLong totalBorrowTime = new AtomicLong(0);
    private final AtomicLong maxBorrowWaitTime = new AtomicLong(0);
    private final AtomicLong totalReturnTime = new AtomicLong(0);
    private final AtomicLong maxReturnWaitTime = new AtomicLong(0);

    private final AtomicLong borrowRateCount = new AtomicLong(0);
    private final AtomicLong returnRateCount = new AtomicLong(0);
    private volatile long lastBorrowRateTime = System.currentTimeMillis();
    private volatile long lastReturnRateTime = System.currentTimeMillis();

    @Override
    /**
     * getActiveCount方法。
     * @return int类型返回值
     */
    public int getActiveCount() {
        return 0; // 由池提供
    }

    @Override
    /**
     * getIdleCount方法。
     * @return int类型返回值
     */
    public int getIdleCount() {
        return 0; // 由池提供
    }

    @Override
    /**
     * getTotalCount方法。
     * @return int类型返回值
     */
    public int getTotalCount() {
        return 0; // 由池提供
    }

    @Override
    /**
     * getWaitingThreads方法。
     * @return int类型返回值
     */
    public int getWaitingThreads() {
        return 0; // 由池提供
    }

    @Override
    /**
     * getBorrowCount方法。
     * @return long类型返回值
     */
    public long getBorrowCount() {
        return borrowCount.get();
    }

    @Override
    /**
     * getReturnCount方法。
     * @return long类型返回值
     */
    public long getReturnCount() {
        return returnCount.get();
    }

    @Override
    /**
     * getCreateCount方法。
     * @return long类型返回值
     */
    public long getCreateCount() {
        return createCount.get();
    }

    @Override
    /**
     * getDestroyCount方法。
     * @return long类型返回值
     */
    public long getDestroyCount() {
        return destroyCount.get();
    }

    @Override
    /**
     * getValidateCount方法。
     * @return long类型返回值
     */
    public long getValidateCount() {
        return validateCount.get();
    }

    @Override
    /**
     * getMaxBorrowWaitTime方法。
     * @return long类型返回值
     */
    public long getMaxBorrowWaitTime() {
        return maxBorrowWaitTime.get();
    }

    @Override
    /**
     * getAvgBorrowWaitTime方法。
     * @return long类型返回值
     */
    public long getAvgBorrowWaitTime() {
        long borrow = borrowCount.get();
        if (borrow == 0) return 0;
        return totalBorrowTime.get() / borrow;
    }

    @Override
    /**
     * getMaxReturnWaitTime方法。
     * @return long类型返回值
     */
    public long getMaxReturnWaitTime() {
        return maxReturnWaitTime.get();
    }

    @Override
    /**
     * getUtilization方法。
     * @return double类型返回值
     */
    public double getUtilization() {
        return 0; // 由池提供
    }

    @Override
    /**
     * getBorrowRate方法。
     * @return double类型返回值
     */
    public double getBorrowRate() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastBorrowRateTime;
        if (elapsed <= 0) return 0;
        long count = borrowRateCount.getAndSet(0);
        lastBorrowRateTime = now;
        return count * 1000.0 / elapsed;
    }

    @Override
    /**
     * getReturnRate方法。
     * @return double类型返回值
     */
    public double getReturnRate() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastReturnRateTime;
        if (elapsed <= 0) return 0;
        long count = returnRateCount.getAndSet(0);
        lastReturnRateTime = now;
        return count * 1000.0 / elapsed;
    }

    // 供池内部调用的方法

    /**
     * recordBorrow方法。
     * * @param waitTime long类型参数
     */
    public void recordBorrow(long waitTime) {
        borrowCount.incrementAndGet();
        borrowRateCount.incrementAndGet();
        totalBorrowTime.addAndGet(waitTime);
        updateMax(maxBorrowWaitTime, waitTime);
    }

    /**
     * recordReturn方法。
     * * @param waitTime long类型参数
     */
    public void recordReturn(long waitTime) {
        returnCount.incrementAndGet();
        returnRateCount.incrementAndGet();
        totalReturnTime.addAndGet(waitTime);
        updateMax(maxReturnWaitTime, waitTime);
    }

    /**
     * recordCreate方法。
     */
    public void recordCreate() {
        createCount.incrementAndGet();
    }

    /**
     * recordDestroy方法。
     */
    public void recordDestroy() {
        destroyCount.incrementAndGet();
    }

    /**
     * recordValidate方法。
     */
    public void recordValidate() {
        validateCount.incrementAndGet();
    }

    private void updateMax(AtomicLong max, long value) {
        long current;
        do {
            current = max.get();
            if (value <= current) break;
        } while (!max.compareAndSet(current, value));
    }

    /**
     * reset方法。
     */
    public void reset() {
        borrowCount.set(0);
        returnCount.set(0);
        createCount.set(0);
        destroyCount.set(0);
        validateCount.set(0);
        totalBorrowTime.set(0);
        maxBorrowWaitTime.set(0);
        totalReturnTime.set(0);
        maxReturnWaitTime.set(0);
    }
}