package com.zifang.util.distributes.sequence;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段模式 ID 生成器（自研，对标美团 Leaf-segment）。
 * <p>
 * 原理：一次性从 DB 拉取一段 [start, end] 区间，本地 AtomicLong 自增。区间用完再去 DB 拉下一段。
 * 优点：
 * <ul>
 *   <li>DB 压力小（一次拉一批）</li>
 *   <li>本地生成是 O(1)，无网络延迟</li>
 *   <li>跨进程不重复（DB 唯一性）</li>
 * </ul>
 * <p>
 * 使用方实现 {@link SegmentLoader}（从 MySQL / 内存等拉号段），传 {@link #SegmentIdGenerator} 即可。
 *
 * <h3>DB 表结构（建议）</h3>
 * <pre>{@code
 *   CREATE TABLE id_segment (
 *       biz_tag   VARCHAR(64)  PRIMARY KEY,
 *       max_id    BIGINT       NOT NULL DEFAULT 1,
 *       step      INT          NOT NULL DEFAULT 1000,
 *       updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
 *   );
 *   -- 拉号段：UPDATE id_segment SET max_id = max_id + step WHERE biz_tag = ?;  SELECT max_id, step ...
 * }</pre>
 */
public class SegmentIdGenerator {

    private final String bizTag;
    private final int step;
    private final SegmentLoader loader;
    private final AtomicLong current;
    private volatile long max;

    public SegmentIdGenerator(String bizTag, int step, SegmentLoader loader) {
        if (bizTag == null || bizTag.isEmpty()) throw new IllegalArgumentException("bizTag");
        if (step <= 0) throw new IllegalArgumentException("step must be > 0");
        if (loader == null) throw new NullPointerException("loader");
        this.bizTag = bizTag;
        this.step = step;
        this.loader = loader;
        this.current = new AtomicLong(0L);
        this.max = 0L;
    }

    public long nextId() {
        // 先取号，再判定是否需要拉新号段
        // current 状态机：
        //   初始：current=0, max=0
        //   拉了第一段 [s,e]：current=s-1, max=e → 下次 incrementAndGet 后 = s
        //   段内继续：current 累加
        //   超出 max：拉新段
        long c = current.incrementAndGet();
        if (c > max) {
            synchronized (this) {
                if (current.get() > max) {
                    long[] seg = loader.loadSegment(bizTag, step);
                    if (seg == null || seg.length != 2 || seg[0] > seg[1]) {
                        throw new IllegalStateException("invalid segment for " + bizTag);
                    }
                    // 设 current = seg[0]-1, max = seg[1]，下次 incrementAndGet = seg[0]
                    current.set(seg[0] - 1);
                    max = seg[1];
                }
            }
            // 重试一次
            c = current.incrementAndGet();
        }
        return c;
    }

    /**
     * 号段加载器 SPI。
     */
    public interface SegmentLoader {
        /**
         * 加载 [start, end] 区间（含两端）。返回 start=0 表示无该业务。
         * <p>
         * 实现示例（MySQL）：
         * <pre>{@code
         *   try (Connection c = ds.getConnection()) {
         *       c.setAutoCommit(false);
         *       try (PreparedStatement upd = c.prepareStatement(
         *               "UPDATE id_segment SET max_id = max_id + step WHERE biz_tag = ?")) {
         *           upd.setString(1, bizTag);
         *           upd.executeUpdate();
         *       }
         *       try (PreparedStatement sel = c.prepareStatement(
         *               "SELECT max_id, step FROM id_segment WHERE biz_tag = ?")) {
         *           sel.setString(1, bizTag);
         *           try (ResultSet rs = sel.executeQuery()) {
         *               if (rs.next()) {
         *                   long maxId = rs.getLong(1);
         *                   int step = rs.getInt(2);
         *                   return new long[]{maxId - step, maxId};
         *               }
         *           }
         *       }
         *       c.commit();
         *   }
         *   return new long[]{0, 0};
         * }</pre>
         */
        long[] loadSegment(String bizTag, int step);
    }
}
