package com.zifang.util.db.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 分布式序号生成器（Snowflake 算法变体）
 * <p>
 * 生成 64 位有序不重复 ID，结构如下：
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  1bit  │   41bit timestamp (ms)    │  10bit nodeId  │  12bit seq    │
 * │  sign  │  2024-01-01 为起始 epoch  │   0 ~ 1023     │  0 ~ 4095/ms  │
 * └──────────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>特点：
 * <ul>
 *   <li>粗有序：ID 随时间递增，但不保证严格顺序</li>
 *   <li>分布式：节点号保证多机不冲突</li>
 *   <li>高性能：本地生成，无网络开销</li>
 *   <li>时钟漂移处理：检测到时钟回拨时等待恢复</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * SnowflakeSequence sequence = new SnowflakeSequence(1);  // 节点号=1
 * long id = sequence.next();
 * </pre>
 *
 * @see Sequence
 */
public class SnowflakeSequence implements Sequence {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeSequence.class);

    // ==================== 位域配置 ====================
    private static final int NODE_BITS = 10;
    private static final int SEQ_BITS = 12;
    private static final int NODE_ID_SHIFT = SEQ_BITS;
    private static final int TIMESTAMP_SHIFT = SEQ_BITS + NODE_BITS;

    // ==================== 约束 ====================
    private static final int MAX_NODE_ID = (1 << NODE_BITS) - 1;       // 1023
    private static final int MAX_SEQ_PER_MS = (1 << SEQ_BITS) - 1;     // 4095

    // ==================== 时间起点 ====================
    private static final long DEFAULT_EPOCH = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    // ==================== 状态 ====================
    private final long nodeId;
    private final long epoch;
    private final AtomicLong sequence = new AtomicLong(0);
    private final Object lock = new Object();
    // ==================== 监控 ====================
    private final AtomicLong totalGenerated = new AtomicLong(0);
    private volatile long lastTimestamp = -1L;

    /**
     * 使用默认 epoch（2024-01-01）和指定节点号创建生成器
     *
     * @param nodeId 节点号（0 ~ 1023），必须全局唯一
     */
    public SnowflakeSequence(long nodeId) {
        this(nodeId, DEFAULT_EPOCH);
    }

    /**
     * 使用指定 epoch 和节点号创建生成器
     *
     * @param nodeId 节点号（0 ~ 1023）
     * @param epoch  起始时间戳（毫秒），通常设为服务上线日期
     */
    public SnowflakeSequence(long nodeId, long epoch) {
        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            throw new IllegalArgumentException(
                    String.format("nodeId 必须在 [%d, %d] 范围内，当前值: %d", 0, MAX_NODE_ID, nodeId));
        }
        if (epoch <= 0) {
            throw new IllegalArgumentException("epoch 必须大于 0，当前值: " + epoch);
        }
        this.nodeId = nodeId;
        this.epoch = epoch;
        log.info("SnowflakeSequence 初始化完成，nodeId={}, epoch={}", nodeId, epoch);
    }

    /**
     * 生成下一个唯一 ID
     */
    @Override
    /**
     * next方法。
     * @return long类型返回值
     */
    public long next() {
        long ts = currentTimestamp();
        long seq;

        synchronized (lock) {
            if (ts < lastTimestamp) {
                ts = waitForNextTimestamp(ts);
            }

            if (ts == lastTimestamp) {
                seq = sequence.incrementAndGet() & MAX_SEQ_PER_MS;
                if (seq == 0) {
                    ts = waitForNextTimestamp(ts);
                    sequence.set(0);
                    seq = sequence.get();
                }
            } else {
                sequence.set(0);
                seq = sequence.get();
            }

            lastTimestamp = ts;
        }

        long id = ((ts - epoch) << TIMESTAMP_SHIFT)
                | (nodeId << NODE_ID_SHIFT)
                | seq;

        totalGenerated.incrementAndGet();
        return id;
    }

    @Override
    /**
     * next方法。
     *      * @param count int类型参数
     * @return long[]类型返回值
     */
    public long[] next(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count 必须大于 0");
        }
        if (count > MAX_SEQ_PER_MS) {
            count = MAX_SEQ_PER_MS;
        }
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = next();
        }
        return ids;
    }

    private long currentTimestamp() {
        return System.currentTimeMillis();
    }

    private long waitForNextTimestamp(long currentTs) {
        long waitTime = lastTimestamp - currentTs;
        if (waitTime > 0) {
            try {
                log.warn("检测到时钟回拨 {}ms，等待恢复...", waitTime);
                lock.wait(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SequenceException("等待时钟恢复时被中断", e);
            }
        }
        long ts = currentTimestamp();
        while (ts <= lastTimestamp) {
            ts = currentTimestamp();
        }
        return ts;
    }

    /**
     * 解析 ID 组成部分
     */
    public String parse(long id) {
        long ts = ((id >> TIMESTAMP_SHIFT) & ~(-1L << 41)) + epoch;
        long nid = (id >> NODE_ID_SHIFT) & MAX_NODE_ID;
        long seq = id & MAX_SEQ_PER_MS;
        String datetime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(ts), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        return String.format("id=%d, datetime=%s, nodeId=%d, seq=%d", id, datetime, nid, seq);
    }

    /**
     * getNodeId方法。
     *
     * @return long类型返回值
     */
    public long getNodeId() {
        return nodeId;
    }

    /**
     * getTotalGenerated方法。
     *
     * @return long类型返回值
     */
    public long getTotalGenerated() {
        return totalGenerated.get();
    }

    /**
     * getTimestampOf方法。
     * * @param id long类型参数
     *
     * @return long类型返回值
     */
    public long getTimestampOf(long id) {
        return ((id >> TIMESTAMP_SHIFT) & ~(-1L << 41)) + epoch;
    }

    /**
     * getNodeIdOf方法。
     * * @param id long类型参数
     *
     * @return long类型返回值
     */
    public long getNodeIdOf(long id) {
        return (id >> NODE_ID_SHIFT) & MAX_NODE_ID;
    }

    /**
     * getSeqOf方法。
     * * @param id long类型参数
     *
     * @return long类型返回值
     */
    public long getSeqOf(long id) {
        return id & MAX_SEQ_PER_MS;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return String.format("SnowflakeSequence[nodeId=%d, epoch=%d, totalGenerated=%d]",
                nodeId, epoch, totalGenerated.get());
    }
}
