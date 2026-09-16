package com.zifang.util.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * WTinyLfuCache 单元测试。
 * <p>
 * W-TinyLFU 自研实现（Window + Main LRU + Count-Min Sketch 准入）。
 * 注意：覆盖基础 CRUD、容量淘汰、过期、监听器、loader 等核心路径；
 * 准入策略（hot 抗扫描）以"频率更高的 key 至少有一个存活"做软断言。
 */
public class WTinyLfuCacheTest {

    private Cache<String, String> cache;

    @Before
    public void setUp() {
        cache = CacheBuilder.<String, String>newBuilder()
                .name("wtinylfu-test")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .maximumSize(100)
                .recordStats()
                .build();
    }

    @After
    public void tearDown() {
        cache.shutdown();
    }

    // ==================== 基础 CRUD ====================

    @Test
    public void testIsInstanceOfWTinyLfu() {
        assertTrue(cache instanceof WTinyLfuCache);
    }

    @Test
    public void testPutAndGet() {
        cache.put("k1", "v1");
        assertEquals("v1", cache.get("k1"));
    }

    @Test
    public void testGetMissReturnsNull() {
        assertNull(cache.get("absent"));
    }

    @Test
    public void testContains() {
        cache.put("k", "v");
        assertTrue(cache.contains("k"));
        assertFalse(cache.contains("absent"));
    }

    @Test
    public void testOverwrite_updatesValue() {
        cache.put("k", "v1");
        cache.put("k", "v2");
        assertEquals("v2", cache.get("k"));
        assertEquals(1, cache.size());
    }

    @Test
    public void testRemove() {
        cache.put("k", "v");
        assertTrue(cache.remove("k"));
        assertNull(cache.get("k"));
        assertFalse(cache.remove("absent"));
    }

    // ==================== null key/value ====================

    @Test(expected = NullPointerException.class)
    public void testPut_nullKeyThrows() {
        cache.put((String) null, "v");
    }

    @Test(expected = NullPointerException.class)
    public void testPut_nullValueThrows() {
        cache.put("k", (String) null);
    }

    // ==================== 容量淘汰 ====================

    @Test
    public void testMaximumSize_evictsWhenExceeded() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("w-evict")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .maximumSize(20)
                .recordStats()
                .build();
        try {
            for (int i = 0; i < 50; i++) c.put("k" + i, "v" + i);
            // W-TinyLFU 在 put 时 size 可能短暂超出（put 后才 evict），但最终应≤ maxSize + window 容差
            int maxAllowed = 20 + Math.max(1, (int) (20 * 0.01)) + 5;
            assertTrue("size=" + c.size() + " should be <= " + maxAllowed, c.size() <= maxAllowed);
            // 至少发生了一次淘汰
            assertTrue("evictions=" + c.stats().evictionCount(),
                    c.stats().evictionCount() >= 1);
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testMaximumSize_hotKeySurvivesScan() {
        // W-TinyLFU 的核心卖点：抗扫描。hot key 频次高，应在 cold 洪水后存活。
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("w-scan")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .maximumSize(200)
                .recordStats()
                .build();
        try {
            // 5 个 hot key，反复访问提升 sketch 频次
            for (int i = 0; i < 5; i++) c.put("hot" + i, "v");
            for (int round = 0; round < 10; round++) {
                for (int i = 0; i < 5; i++) c.get("hot" + i);
            }
            // 洪水：500 个 cold key（每个只 put 一次，freq=1）
            for (int i = 0; i < 500; i++) c.put("cold" + i, "v");
            // 至少 1 个 hot key 应当存活（sketch 准入保护）
            int hotSurvived = 0;
            for (int i = 0; i < 5; i++) {
                if (c.contains("hot" + i)) hotSurvived++;
            }
            assertTrue("at least one hot key should survive scan, got " + hotSurvived, hotSurvived >= 1);
        } finally {
            c.shutdown();
        }
    }

    // ==================== 过期 ====================

    @Test
    public void testExpireAfterWrite() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("w-exp-write")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .expireAfterWrite(Duration.ofMillis(200))
                .recordStats()
                .build();
        try {
            c.put("k", "v");
            assertEquals("v", c.get("k"));
            Thread.sleep(400);
            assertNull(c.get("k"));
            assertTrue(c.stats().expirationCount() >= 1);
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testExpireAfterAccess_resetsOnGet() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("w-exp-access")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .expireAfterAccess(Duration.ofMillis(200))
                .build();
        try {
            c.put("k", "v");
            Thread.sleep(100);
            assertEquals("v", c.get("k")); // 刷新 access
            Thread.sleep(150);
            // 总 250ms，但最后一次访问 150ms 前，未过期
            assertEquals("v", c.get("k"));
            Thread.sleep(250);
            assertNull(c.get("k"));
        } finally {
            c.shutdown();
        }
    }

    // ==================== 监听器 ====================

    @Test
    public void testListener_explicit() {
        AtomicInteger calls = new AtomicInteger();
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("w-listener")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .addListener(n -> {
                    if (n.getCause() == RemovalListener.RemovalCause.EXPLICIT) calls.incrementAndGet();
                })
                .build();
        try {
            c.put("k", "v");
            c.remove("k");
            assertEquals(1, calls.get());
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testListener_sizeLimit_onEviction() {
        AtomicInteger evictions = new AtomicInteger();
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("w-evict-listener")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .maximumSize(5)
                .addListener(n -> {
                    if (n.getCause() == RemovalListener.RemovalCause.SIZE_LIMIT) evictions.incrementAndGet();
                })
                .build();
        try {
            for (int i = 0; i < 20; i++) c.put("k" + i, "v");
            assertTrue("evictions=" + evictions.get(), evictions.get() >= 1);
        } finally {
            c.shutdown();
        }
    }

    // ==================== get-with-loader ====================

    @Test
    public void testGetWithLoader_loadsOnMiss() {
        AtomicInteger calls = new AtomicInteger();
        String v = cache.get("k", k -> {
            calls.incrementAndGet();
            return "loaded-" + k;
        });
        assertEquals("loaded-k", v);
        assertEquals(1, calls.get());
        // 第二次走缓存
        assertEquals("loaded-k", cache.get("k"));
        assertEquals(1, calls.get());
    }

    @Test
    public void testGetWithLoader_exceptionPropagates() {
        try {
            cache.get("k", k -> {
                throw new IllegalStateException("loader boom");
            });
            fail("expected exception");
        } catch (IllegalStateException expected) {
            assertEquals("loader boom", expected.getMessage());
        }
        assertTrue("loadFailure should be recorded",
                cache.stats().loadFailureCount() >= 1);
    }

    // ==================== getOrLoad（WTinyLfuCache 特有方法） ====================

    @Test
    public void testGetOrLoad_loadsOnMiss() {
        // 强转为 WTinyLfuCache 访问 getOrLoad
        WTinyLfuCache<String, String> w = (WTinyLfuCache<String, String>) cache;
        AtomicInteger calls = new AtomicInteger();
        String v = w.getOrLoad("k", k -> {
            calls.incrementAndGet();
            return "v-" + k;
        });
        assertEquals("v-k", v);
        assertEquals(1, calls.get());
    }

    // ==================== put(key, loader) ====================

    @Test(expected = NullPointerException.class)
    public void testPutWithLoader_nullLoader_throwsNPE() {
        // WTinyLfuCache.put(K, Function) 不校验 null loader，直接 NPE
        cache.put("k", (java.util.function.Function<String, String>) null);
    }

    @Test
    public void testPutWithLoader_returnsNull_doesNotCache() {
        AtomicInteger calls = new AtomicInteger();
        cache.put("k", k -> {
            calls.incrementAndGet();
            return null;
        });
        assertEquals(1, calls.get());
        assertFalse(cache.contains("k"));
    }

    // ==================== 批量 ====================

    @Test
    public void testGetAllPresent_skipsMisses() {
        cache.put("a", "1");
        cache.put("b", "2");
        Map<String, String> got = cache.getAllPresent(java.util.Arrays.asList("a", "absent", "b"));
        assertEquals(2, got.size());
        assertEquals("1", got.get("a"));
        assertEquals("2", got.get("b"));
    }

    @Test
    public void testPutAll() {
        Map<String, String> data = new HashMap<>();
        data.put("a", "1");
        data.put("b", "2");
        cache.putAll(data);
        assertEquals("1", cache.get("a"));
        assertEquals("2", cache.get("b"));
    }

    // ==================== asMap ====================

    @Test
    public void testAsMap_snapshot() {
        cache.put("a", "1");
        cache.put("b", "2");
        Map<String, String> view = cache.asMap();
        assertEquals(2, view.size());
        assertEquals("1", view.get("a"));
        // WTinyLfuCache 返回的是 unmodifiableMap
        try {
            view.put("c", "3");
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) { /* ok */ }
    }

    // ==================== 统计 ====================

    @Test
    public void testStats_missOnAbsent() {
        // WTinyLfuCache.get 在 key 缺失时 recordMiss
        // 注意：当前实现在常规命中路径不调用 recordHit（只对 null sentinel 记录 hit）
        cache.get("absent");
        assertEquals(1L, cache.stats().missCount());
    }

    @Test
    public void testStats_evictionCounted() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("w-stats-evict")
                .algorithm(CacheBuilder.Algorithm.WTINYLFU)
                .maximumSize(5)
                .recordStats()
                .build();
        try {
            for (int i = 0; i < 30; i++) c.put("k" + i, "v");
            assertTrue("evictions=" + c.stats().evictionCount(), c.stats().evictionCount() >= 1);
        } finally {
            c.shutdown();
        }
    }

    // ==================== name ====================

    @Test
    public void testName() {
        assertEquals("wtinylfu-test", cache.getName());
    }

    // ==================== shutdown ====================

    @Test
    public void testShutdown_idempotent() {
        cache.shutdown();
        // 再次调用不应抛错
        cache.shutdown();
    }

    @Test(expected = IllegalStateException.class)
    public void testShutdown_thenPutThrows() {
        cache.shutdown();
        cache.put("k", "v");
    }

    @Test(expected = IllegalStateException.class)
    public void testShutdown_thenGetThrows() {
        cache.shutdown();
        cache.get("k");
    }
}
