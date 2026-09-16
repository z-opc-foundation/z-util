package com.zifang.util.distributes.sequence;


/**
 * 分布式高效有序ID生产黑科技
 * <p>
 * 基于Twitter的Snowflake算法实现，用于在分布式环境中生成全局唯一的有序ID。
 * ID由时间戳、机器ID、数据中心ID和序列号组成，共64位。
 *
 * @author zifang
 */
public class Sequence {

    private final long twepoch = 1288834974657L;
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数，初始化分布式ID生成器
     *
     * @param workerId     工作机器ID，有效范围：0 ~ 31
     * @param datacenterId 数据中心ID，有效范围：0 ~ 31
     * @throws IllegalArgumentException 如果workerId或datacenterId超出有效范围
     */
    public Sequence(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获取下一个全局唯一的有序ID
     * <p>
     * 该方法是线程安全的，同一时刻只有一个线程能生成ID。
     * 如果当前时间小于上一次生成ID的时间戳，会抛出异常（系统时钟回拨）。
     *
     * @return 返回一个64位的long类型唯一ID
     * @throws RuntimeException 如果系统时钟回拨，会拒绝生成ID并抛出异常
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift) | sequence;
    }

    /**
     * 阻塞到获取下一个毫秒的时间戳
     * <p>
     * 当同一毫秒内序列号用尽时，调用此方法等待下一个毫秒。
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一个可用的时间戳（毫秒）
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳（毫秒）
     * <p>
     * 使用SystemClock获取时间，比直接调用System.currentTimeMillis()性能更好。
     *
     * @return 当前时间戳（毫秒）
     */
    protected long timeGen() {
        return SystemClock.now();
    }

}