package com.zifang.util.cache;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

/**
 * CacheBuilder 单元测试：构造参数校验 + 默认值 + build(loader) 行为。
 */
public class CacheBuilderTest {

    // ==================== name 校验 ====================

    @Test(expected = IllegalArgumentException.class)
    public void testName_nullThrows() {
        CacheBuilder.<String, String>newBuilder().name(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testName_emptyThrows() {
        CacheBuilder.<String, String>newBuilder().name("");
    }

    @Test
    public void testName_defaultNotEmpty() {
        // 不调用 name() 时，构造器会生成一个非空的默认名
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder().build();
        assertNotNull(c.getName());
        assertFalse(c.getName().isEmpty());
        c.shutdown();
    }

    @Test
    public void testName_appliedCorrectly() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder().name("users").build();
        assertEquals("users", c.getName());
        c.shutdown();
    }

    // ==================== initialCapacity 校验 ====================

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCapacity_zeroThrows() {
        CacheBuilder.<String, String>newBuilder().initialCapacity(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitialCapacity_negativeThrows() {
        CacheBuilder.<String, String>newBuilder().initialCapacity(-5);
    }

    // ==================== maximumSize 校验 ====================

    @Test
    public void testMaximumSize_negativeOneMeansUnbounded() {
        // -1 是合法的（表示无界）
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .maximumSize(-1)
                .build();
        assertNotNull(c);
        c.shutdown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaximumSize_negativeOtherThanMinusOneThrows() {
        CacheBuilder.<String, String>newBuilder().maximumSize(-2);
    }

    @Test
    public void testMaximumSize_zero_meansHardLimitZero() {
        // maximumSize=0 等价于"不允许任何条目"（data.size() > 0 立即淘汰）
        // 注意：0 不是无界，无界用 -1 表示
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .maximumSize(0)
                .build();
        c.put("a", "1");
        // put 触发 enforceCapacity → 立即淘汰
        assertEquals(0, c.size());
        c.shutdown();
    }

    // ==================== expire* 配置 ====================

    @Test
    public void testExpireAfterWrite_nullMeansNever() {
        // null → -1（不过期）
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .expireAfterWrite((Duration) null)
                .build();
        c.put("k", "v");
        assertEquals("v", c.get("k"));
        c.shutdown();
    }

    @Test
    public void testExpireAfterAccess_nullMeansNever() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .expireAfterAccess((Duration) null)
                .build();
        c.put("k", "v");
        assertEquals("v", c.get("k"));
        c.shutdown();
    }

    @Test
    public void testRefreshAfterWrite_nullMeansNever() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .refreshAfterWrite((Duration) null)
                .build();
        c.put("k", "v");
        assertEquals("v", c.get("k"));
        c.shutdown();
    }

    // ==================== build(loader) 校验 ====================

    @Test(expected = NullPointerException.class)
    public void testBuildWithNullLoader_throws() {
        CacheBuilder.<String, String>newBuilder().build((CacheLoader<String, String>) null);
    }

    @Test
    public void testBuildWithLoader_returnsLoadingCache() {
        LoadingCache<String, String> lc = CacheBuilder.<String, String>newBuilder()
                .name("loading")
                .build(k -> "v-" + k);
        assertTrue(lc instanceof LoadingCache);
        assertEquals("v-k", lc.get("k"));
        lc.shutdown();
    }

    // ==================== addListener ====================

    @Test
    public void testAddListener_nullIgnored() {
        // 传 null 不抛错，listeners 保持空
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .addListener(null)
                .build();
        assertTrue(c.removalListeners().isEmpty());
        c.shutdown();
    }

    @Test
    public void testAddListener_dedupByHashSet() {
        RemovalListener<String, String> listener = n -> { };
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .addListener(listener)
                .addListener(listener)   // 重复
                .build();
        // Set 去重
        assertEquals(1, c.removalListeners().size());
        c.shutdown();
    }

    // ==================== algorithm 切换 ====================

    @Test
    public void testAlgorithm_defaultIsLru() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder().build();
        // 默认 LRU → MemoryCache
        assertTrue(c instanceof MemoryCache);
        c.shutdown();
    }

    @Test
    public void testAlgorithm_wtinyLfu() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .maximumSize(10)
                .build();
        assertTrue(c instanceof WTinyLfuCache);
        c.shutdown();
    }

    @Test
    public void testAlgorithm_explicitLru() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .algorithm(CacheBuilder.Algorithm.LRU)
                .build();
        assertTrue(c instanceof MemoryCache);
        c.shutdown();
    }

    // ==================== Algorithm 枚举 ====================

    @Test
    public void testAlgorithmEnum_values() {
        // 保证枚举完整性
        assertEquals(2, CacheBuilder.Algorithm.values().length);
        assertNotNull(CacheBuilder.Algorithm.valueOf("LRU"));
        assertNotNull(CacheBuilder.Algorithm.valueOf("WTINYLFU"));
    }

    // ==================== 链式调用返回自身 ====================

    @Test
    public void testChainedCalls_returnSameBuilder() {
        CacheBuilder<String, String> b = CacheBuilder.newBuilder();
        assertSame(b, b.name("n"));
        assertSame(b, b.initialCapacity(16));
        assertSame(b, b.maximumSize(10));
        assertSame(b, b.expireAfterWrite(Duration.ofMinutes(1)));
        assertSame(b, b.expireAfterAccess(Duration.ofMinutes(1)));
        assertSame(b, b.refreshAfterWrite(Duration.ofMinutes(1)));
        assertSame(b, b.recordStats());
        assertSame(b, b.addListener(n -> { }));
        assertSame(b, b.nullValueProtection());
        assertSame(b, b.algorithm(CacheBuilder.Algorithm.LRU));
    }

    // ==================== recordStats ====================

    @Test
    public void testRecordStats_disabledByDefault_MemoryCache_returnsStatsInstance() {
        // MemoryCache.stats() 始终返回 CacheStats 实例（不论 recordStats）
        // 这是 MemoryCache 的实现选择；WTinyLfuCache 则返回 null
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder().build();
        assertTrue(c instanceof MemoryCache);
        assertNotNull(c.stats());
        c.shutdown();
    }

    @Test
    public void testRecordStats_disabledByDefault_WTinyLfu_returnsNull() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .build();
        // WTinyLfuCache 在 recordStats=false 时 stats() 返回 null
        assertTrue(c instanceof WTinyLfuCache);
        assertNull(c.stats());
        c.shutdown();
    }

    @Test
    public void testRecordStats_enabledReturnsInstance() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .recordStats()
                .build();
        assertNotNull(c.stats());
        c.shutdown();
    }
}
