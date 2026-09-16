package com.zifang.util.core.lang;

import com.zifang.util.core.net.NetworkUtil;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * @author: zifang
 * @time: 2019-09-17 19:26:00
 * @description: snowFlakeIdWorker
 */
public class SnowFlakeIdWorker {

    /**
     * 开始时间截 (2019-01-01)
     */
    private static final long TW_EPOCH = 1546272_000_000L;

    /**
     * 机器id所占的位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据标识id所占的位数
     */
    private static final long DATA_CENTER_ID_BITS = 5L;

    /**
     * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 支持的最大数据标识id，结果是31
     */
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    /**
     * 序列在id中占的位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID向左移12位
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 数据标识id向左移17位(12+5)
     */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 时间截向左移22位(5+5+12)
     */
    private static final long TIMESTAMP_LEFT_SHIFT =
            SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    /**
     * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    // 记录近2S的毫秒数的sequence的缓存
    private static final int LENGTH = 2000;
    /**
     * 工作机器ID(0~31)
     */
    private final long workerId;
    /**
     * 数据中心ID(0~31)
     */
    private final long dataCenterId;
    // sequence缓存
    private final long[] sequenceCycle = new long[LENGTH];
    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    /**
     * 上次生成ID的时间截
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     */
    private SnowFlakeIdWorker() {
        this(getWorkId());
    }

    /**
     * 构造函数
     *
     * @param workerId 工作ID (0~31)
     */
    private SnowFlakeIdWorker(long workerId) {
        this(workerId, 0);
    }

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param dataCenterId 数据中心ID (0~31)
     */
    public SnowFlakeIdWorker(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("DataCenter Id can't be greater than %d or less than 0",
                            MAX_DATA_CENTER_ID));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * @author: zifang
     * @description: 获取工作ID
     * @time: 2020/9/1 15:26
     * @params: [] 请求参数
     * @return: java.lang.Long 响应参数
     */
    protected static Long getWorkId() {
        int[] macArr;
        try {
            macArr = toCodePoints(NetworkUtil.getMac());
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException("SnowFlake Id worker get mac fail", e);
        }
        int sums = 0;
        for (int i : Objects.requireNonNull(macArr)) {
            sums += i;
        }
        return (long) (sums % 32);
    }

    private static int[] toCodePoints(CharSequence str) {
        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            return new int[0];
        } else {
            String s = str.toString();
            int[] result = new int[s.codePointCount(0, s.length())];
            int index = 0;

            for (int i = 0; i < result.length; ++i) {
                result[i] = s.codePointAt(index);
                index += Character.charCount(result[i]);
            }

            return result;
        }
    }

    /**
     * getInstance方法。
     *
     * @return static SnowFlakeIdWorker类型返回值
     */
    public static SnowFlakeIdWorker getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                    "Clock moved backwards, refusing to generate id for %d ms, last time is %d ms, current time is %d ms.",
                    lastTimestamp - timestamp, lastTimestamp, timestamp));
        }

        // 如果是同一时间生成的，则进行毫秒内序列重置
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return allocate(timestamp - TW_EPOCH);
    }

    /**
     * nextIdByCacheWhenClockMoved方法。
     *
     * @return synchronized long类型返回值
     */
    public synchronized long nextIdByCacheWhenClockMoved() {
        long timestamp = timeGen();
        int index = (int) (timestamp % LENGTH);
        // 出现时钟回拨问题，获取历史序列号自增
        if (timestamp < lastTimestamp) {
            long tempSequence;
            do {
                if ((lastTimestamp - timestamp) > LENGTH) {
                    // 可自定义异常、告警等，短暂不能对外提供，故障转移，将请求转发到正常机器。
                    throw new UnsupportedOperationException(
                            String.format("The time back range is too large and exceeds %dms caches", LENGTH));
                }
                long preSequence = sequenceCycle[index];
                tempSequence = (preSequence + 1) & SEQUENCE_MASK;
                if (tempSequence == 0) {
                    // 如果取出的历史序列号+1后已经达到超过最大值，
                    // 则重新获取timestamp,重新拿其他位置的缓存
                    timestamp = tilNextMillis(lastTimestamp);
                    index = (int) (timestamp % LENGTH);
                } else {
                    // 更新缓存
                    sequenceCycle[index] = tempSequence;
                    return allocate((timestamp - TW_EPOCH), tempSequence);
                }
            } while (timestamp < lastTimestamp);
            // 如果在获取缓存的过程中timestamp恢复正常了，就走正常流程
        }
        // 时间等于lastTimestamp，取当前的sequence + 1
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // Exceed the max sequence, we wait the next second to generate id
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
                index = (int) (timestamp % LENGTH);
            }
        } else {
            // 时间大于lastTimestamp没有发生回拨， sequence 从0开始
            sequence = 0L;
        }
        // 缓存sequence + 更新lastTimestamp
        sequenceCycle[index] = sequence;
        lastTimestamp = timestamp;
        // 生成id
        return allocate(timestamp - TW_EPOCH);
    }

    private long allocate(long deltaSeconds) {
        return allocate(deltaSeconds, sequence);
    }

    private long allocate(long deltaSeconds, long sequence) {
        return (deltaSeconds << TIMESTAMP_LEFT_SHIFT) | (dataCenterId << DATA_CENTER_ID_SHIFT) | (
                workerId << WORKER_ID_SHIFT) | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    private static class LazyHolder {

        private static final SnowFlakeIdWorker INSTANCE = new SnowFlakeIdWorker();
    }

}
