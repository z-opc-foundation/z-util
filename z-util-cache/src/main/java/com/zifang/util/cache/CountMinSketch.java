package com.zifang.util.cache;

/**
 * 4-bit Count-Min Sketch 频次近似器（自研，零三方）。
 * <p>
 * 原理：用若干行（默认 4 行）的小计数器数组记录 key 的访问频次。增加时所有行 +1（饱和到 15），
 * 估时取所有行的最小值（容忍哈希冲突）。定期对所有计数器除以 2（reset）以"忘记"陈旧频次，
 * 模拟时间衰减。
 * <p>
 * 4 bit = 单格容量 0..15。比 Caffeine 的实现简单但语义一致。
 */
final class CountMinSketch {

    /**
     * 一行的宽度（必须是 2 的幂，便于位运算取模）
     */
    private final int tableMask;
    /**
     * 计数器数组（一维：[row][col]）
     */
    private final long[] table;          // 每格 4 bit → 一个 long 装 16 格
    private final int tableLength;       // 计数器格数
    private final int sampleSize;        // 重置阈值
    private int sampleCount;             // 距离上次 reset 的累加次数
    private int totalCount;              // 累计 increment 次数

    CountMinSketch(int tableSize, int sampleSize) {
        if (Integer.bitCount(tableSize) != 1) {
            throw new IllegalArgumentException("tableSize must be power of 2, got " + tableSize);
        }
        this.tableLength = tableSize;
        this.tableMask = tableSize - 1;
        // 4 rows
        this.table = new long[4 * tableSize];
        this.sampleSize = sampleSize;
    }

    private static int spread(int x) {
        x = ((x >>> 16) ^ x) * 0x45d9f3b;
        return (x ^ (x >>> 16));
    }

    /**
     * 估频次：4 个 hash 位置取最小值。
     */
    int estimate(Object key) {
        int h1 = spread(key.hashCode());
        int h2 = h1 >>> 16;
        int min = 15;
        for (int i = 0; i < 4; i++) {
            int idx = indexOf(h1, h2, i);
            int v = (int) ((table[i * tableLength + (idx >>> 4)] >>> ((idx & 15) << 2)) & 0xFL);
            if (v < min) min = v;
        }
        return min;
    }

    /**
     * 频次 +1（饱和到 15）。
     */
    void increment(Object key) {
        int h1 = spread(key.hashCode());
        int h2 = h1 >>> 16;
        boolean added = false;
        for (int i = 0; i < 4; i++) {
            int idx = indexOf(h1, h2, i);
            int slotIdx = i * tableLength + (idx >>> 4);
            int shift = (idx & 15) << 2;
            long mask = 0xFL << shift;
            int v = (int) ((table[slotIdx] >>> shift) & 0xFL);
            if (v < 15) {
                table[slotIdx] = (table[slotIdx] & ~mask) | ((long) (v + 1) << shift);
                added = true;
            }
        }
        // sampleCount 总是 +1；只有真正增加了频次才视为"有效 increment"
        totalCount++;
        if (added) {
            sampleCount++;
            if (sampleCount >= sampleSize) {
                reset();
            }
        }
    }

    /**
     * 测试用：当前 sampleCount。
     */
    int sampleCount() {
        return sampleCount;
    }

    /**
     * 所有计数器右移 1 位（除以 2），实现时间衰减。
     */
    void reset() {
        int count = 0;
        for (int i = 0; i < table.length; i++) {
            long v = table[i];
            if (v != 0) {
                table[i] = (v >>> 1) & 0x7777777777777777L;  // 4-bit 半减法
                count++;
            }
        }
        sampleCount = 0;
    }

    int totalCount() {
        return totalCount;
    }

    // ===== 工具 =====
    private int indexOf(int h1, int h2, int row) {
        int h = h1 + (row * h2);
        h = (h ^ (h >>> 16)) * 0x85ebca6b;
        h = (h ^ (h >>> 13)) * 0xc2b2ae35;
        return (h ^ (h >>> 16)) & tableMask;
    }
}
