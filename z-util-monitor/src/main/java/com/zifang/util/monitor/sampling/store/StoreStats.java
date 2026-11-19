package com.zifang.util.monitor.sampling.store;

/**
 * 存储统计。
 *
 * @author zifang
 */
public final class StoreStats {

    private final long totalAppended;
    private final long totalEvicted;
    private final long totalQueried;
    private final int currentSize;
    private final long estimatedBytes;

    /**
     * StoreStats方法。
     *
     * @param totalAppended  long类型参数
     * @param totalEvicted   long类型参数
     * @param totalQueried   long类型参数
     * @param currentSize    int类型参数
     * @param estimatedBytes long类型参数
     */
    public StoreStats(long totalAppended, long totalEvicted, long totalQueried,
                      int currentSize, long estimatedBytes) {
        this.totalAppended = totalAppended;
        this.totalEvicted = totalEvicted;
        this.totalQueried = totalQueried;
        this.currentSize = currentSize;
        this.estimatedBytes = estimatedBytes;
    }

    /**
     * getTotalAppended方法。
     *
     * @return long类型返回值
     */
    public long getTotalAppended() {
        return totalAppended;
    }

    /**
     * getTotalEvicted方法。
     *
     * @return long类型返回值
     */
    public long getTotalEvicted() {
        return totalEvicted;
    }

    /**
     * getTotalQueried方法。
     *
     * @return long类型返回值
     */
    public long getTotalQueried() {
        return totalQueried;
    }

    /**
     * getCurrentSize方法。
     *
     * @return int类型返回值
     */
    public int getCurrentSize() {
        return currentSize;
    }

    /**
     * getEstimatedBytes方法。
     *
     * @return long类型返回值
     */
    public long getEstimatedBytes() {
        return estimatedBytes;
    }

    @Override
    public String toString() {
        return "StoreStats{appended=" + totalAppended + ", evicted=" + totalEvicted
                + ", queried=" + totalQueried + ", size=" + currentSize
                + ", bytes=" + estimatedBytes + '}';
    }
}
