package com.zifang.util.cache;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CacheStats 单元测试：各计数器、命中率、平均加载耗时、recordRemoval 按 cause 分类、toString。
 */
public class CacheStatsTest {

    private CacheStats stats;

    @Before
    public void setUp() {
        stats = new CacheStats();
    }

    // ==================== 计数器累加 ====================

    @Test
    public void testInitialAllZero() {
        assertEquals(0L, stats.hitCount());
        assertEquals(0L, stats.missCount());
        assertEquals(0L, stats.loadSuccessCount());
        assertEquals(0L, stats.loadFailureCount());
        assertEquals(0L, stats.evictionCount());
        assertEquals(0L, stats.expirationCount());
        assertEquals(0L, stats.totalLoadTimeNanos());
    }

    @Test
    public void testRecordHit_andMiss() {
        stats.recordHit();
        stats.recordHit();
        stats.recordMiss();
        assertEquals(2L, stats.hitCount());
        assertEquals(1L, stats.missCount());
    }

    @Test
    public void testRecordLoadSuccess_addsTime() {
        stats.recordLoadSuccess(100L);
        stats.recordLoadSuccess(200L);
        assertEquals(2L, stats.loadSuccessCount());
        assertEquals(300L, stats.totalLoadTimeNanos());
    }

    @Test
    public void testRecordLoadFailure_addsTime() {
        stats.recordLoadFailure(50L);
        stats.recordLoadFailure(70L);
        assertEquals(2L, stats.loadFailureCount());
        assertEquals(120L, stats.totalLoadTimeNanos());
    }

    @Test
    public void testRecordEviction() {
        stats.recordEviction();
        stats.recordEviction();
        stats.recordEviction();
        assertEquals(3L, stats.evictionCount());
    }

    @Test
    public void testRecordExpiration() {
        stats.recordExpiration();
        assertEquals(1L, stats.expirationCount());
    }

    // ==================== recordRemoval 按 cause 分类 ====================

    @Test
    public void testRecordRemoval_sizeLimit_countsAsEviction() {
        stats.recordRemoval(RemovalListener.RemovalCause.SIZE_LIMIT);
        stats.recordRemoval(RemovalListener.RemovalCause.COLLECTED);
        assertEquals(2L, stats.evictionCount());
        assertEquals(0L, stats.expirationCount());
    }

    @Test
    public void testRecordRemoval_expired_countsAsExpiration() {
        stats.recordRemoval(RemovalListener.RemovalCause.EXPIRED);
        stats.recordRemoval(RemovalListener.RemovalCause.ACCESS_EXPIRED);
        stats.recordRemoval(RemovalListener.RemovalCause.REPLACED);
        assertEquals(3L, stats.expirationCount());
        assertEquals(0L, stats.evictionCount());
    }

    @Test
    public void testRecordRemoval_explicit_noCount() {
        // EXPLICIT 不单独计数
        stats.recordRemoval(RemovalListener.RemovalCause.EXPLICIT);
        assertEquals(0L, stats.evictionCount());
        assertEquals(0L, stats.expirationCount());
    }

    @Test
    public void testRecordRemoval_null_ignored() {
        stats.recordRemoval(null);
        assertEquals(0L, stats.evictionCount());
        assertEquals(0L, stats.expirationCount());
    }

    // ==================== 命中率 ====================

    @Test
    public void testHitRate_noRequests_returnsZero() {
        assertEquals(0.0, stats.hitRate(), 0.0001);
    }

    @Test
    public void testHitRate_allHits() {
        for (int i = 0; i < 10; i++) stats.recordHit();
        assertEquals(1.0, stats.hitRate(), 0.0001);
    }

    @Test
    public void testHitRate_allMisses() {
        for (int i = 0; i < 10; i++) stats.recordMiss();
        assertEquals(0.0, stats.hitRate(), 0.0001);
    }

    @Test
    public void testHitRate_mixed() {
        stats.recordHit();
        stats.recordHit();
        stats.recordHit();
        stats.recordMiss();
        assertEquals(0.75, stats.hitRate(), 0.0001);
    }

    // ==================== 平均加载耗时 ====================

    @Test
    public void testAverageLoadPenalty_noLoads_returnsZero() {
        assertEquals(0.0, stats.averageLoadPenaltyNanos(), 0.0001);
    }

    @Test
    public void testAverageLoadPenalty_successOnly() {
        stats.recordLoadSuccess(100L);
        stats.recordLoadSuccess(300L);
        assertEquals(200.0, stats.averageLoadPenaltyNanos(), 0.0001);
    }

    @Test
    public void testAverageLoadPenalty_mixedSuccessAndFailure() {
        stats.recordLoadSuccess(100L);
        stats.recordLoadFailure(200L);
        // (100+200)/2 = 150
        assertEquals(150.0, stats.averageLoadPenaltyNanos(), 0.0001);
    }

    // ==================== toString ====================

    @Test
    public void testToString_containsKeyFields() {
        stats.recordHit();
        stats.recordMiss();
        String s = stats.toString();
        assertNotNull(s);
        assertTrue(s.contains("hit=1"));
        assertTrue(s.contains("miss=1"));
        assertTrue(s.contains("hitRate"));
    }

    // ==================== 并发安全（粗略） ====================

    @Test
    public void testConcurrentHitIncrement() throws InterruptedException {
        final int threads = 8;
        final int perThread = 1000;
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < perThread; j++) stats.recordHit();
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        assertEquals((long) threads * perThread, stats.hitCount());
    }
}
