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
 * MemoryCache / LoadingCache 单元测试。
 */
public class MemoryCacheTest {

    private Cache<String, String> cache;

    @Before
    public void setUp() {
        cache = CacheBuilder.<String, String>newBuilder()
                .name("test")
                .recordStats()
                .build();
    }

    @After
    public void tearDown() {
        cache.shutdown();
    }

    // ==================== 基础 CRUD ====================

    @Test
    public void testName() {
        assertEquals("test", cache.getName());
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
    public void testOverwrite() {
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

    @Test
    public void testClear() {
        cache.put("a", "1");
        cache.put("b", "2");
        cache.invalidateAll();
        assertEquals(0, cache.size());
    }

    // ==================== 过期 ====================

    @Test
    public void testExpireAfterWrite() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("exp-write")
                .expireAfterWrite(Duration.ofMillis(300))
                .recordStats()
                .build();
        c.put("k", "v");
        assertEquals("v", c.get("k"));
        Thread.sleep(500);
        assertNull(c.get("k"));
        assertTrue(c.stats().expirationCount() >= 1);
        c.shutdown();
    }

    @Test
    public void testExpireAfterAccess_resetsOnGet() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("exp-access")
                .expireAfterAccess(Duration.ofMillis(300))
                .build();
        c.put("k", "v");
        Thread.sleep(150);
        // 在过期前 get 一下，刷新访问时间
        assertEquals("v", c.get("k"));
        Thread.sleep(200);
        // 总共 350ms，但最后一次访问是 200ms 前，没过期
        assertEquals("v", c.get("k"));
        Thread.sleep(350);
        assertNull(c.get("k"));
        c.shutdown();
    }

    // ==================== LRU 淘汰 ====================

    @Test
    public void testMaximumSize_evictsLRU() {
        Cache<Integer, String> c = CacheBuilder.<Integer, String>newBuilder()
                .name("lru")
                .maximumSize(3)
                .recordStats()
                .build();
        c.put(1, "a");
        c.put(2, "b");
        c.put(3, "c");
        c.get(1); // 访问 1，让 2 变 LRU
        c.put(4, "d"); // 触发淘汰
        assertNull(c.get(2));
        assertNotNull(c.get(1));
        assertNotNull(c.get(3));
        assertNotNull(c.get(4));
        assertTrue(c.stats().evictionCount() >= 1);
        c.shutdown();
    }

    // ==================== 批量 ====================

    @Test
    public void testGetAllPresent_skipsMiss() {
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

    // ==================== 统计 ====================

    @Test
    public void testStats_hitMiss() {
        cache.put("k", "v");
        cache.get("k");     // hit
        cache.get("miss"); // miss
        assertEquals(1, cache.stats().hitCount());
        assertEquals(1, cache.stats().missCount());
        assertEquals(0.5, cache.stats().hitRate(), 0.0001);
    }

    // ==================== 监听器 ====================

    @Test
    public void testRemovalListener_explicit() {
        AtomicInteger calls = new AtomicInteger();
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("listener")
                .addListener(n -> {
                    if (n.getCause() == RemovalListener.RemovalCause.EXPLICIT) calls.incrementAndGet();
                })
                .build();
        c.put("k", "v");
        c.remove("k");
        assertEquals(1, calls.get());
        c.shutdown();
    }

    @Test
    public void testRemovalListener_eviction() {
        AtomicInteger evictions = new AtomicInteger();
        Cache<Integer, String> c = CacheBuilder.<Integer, String>newBuilder()
                .name("evict-listener")
                .maximumSize(2)
                .addListener(n -> {
                    if (n.getCause() == RemovalListener.RemovalCause.SIZE_LIMIT) evictions.incrementAndGet();
                })
                .build();
        c.put(1, "a");
        c.put(2, "b");
        c.put(3, "c"); // 触发淘汰
        c.put(4, "d"); // 再触发一次
        assertEquals(2, evictions.get());
        c.shutdown();
    }

    // ==================== get-or-load ====================

    @Test
    public void testGetWithLoader_miss() {
        AtomicInteger calls = new AtomicInteger();
        String v = cache.get("k", k -> {
            calls.incrementAndGet();
            return "loaded-" + k;
        });
        assertEquals("loaded-k", v);
        assertEquals(1, calls.get());
        // 第二次取，loader 不再被调
        assertEquals("loaded-k", cache.get("k"));
        assertEquals(1, calls.get());
    }

    @Test
    public void testGetWithLoader_loaderReturnsNull_doesNotCache() {
        AtomicInteger calls = new AtomicInteger();
        cache.get("k", k -> {
            calls.incrementAndGet();
            return null;
        });
        assertEquals(1, calls.get());
        // 第二次再 miss，会再次调 loader
        cache.get("k", k -> {
            calls.incrementAndGet();
            return null;
        });
        assertEquals(2, calls.get());
    }

    // ==================== asMap ====================

    @Test
    public void testAsMap_isView() {
        cache.put("a", "1");
        cache.put("b", "2");
        Map<String, String> view = cache.asMap();
        assertEquals(2, view.size());
        assertEquals("1", view.get("a"));
    }

    // ==================== LoadingCache ====================

    @Test
    public void testLoadingCache_autoLoad() {
        AtomicInteger calls = new AtomicInteger();
        LoadingCache<String, String> lc = CacheBuilder.<String, String>newBuilder()
                .name("loading")
                .build(k -> {
                    calls.incrementAndGet();
                    return "v-" + k;
                });
        assertEquals("v-1", lc.get("1"));
        assertEquals("v-1", lc.get("1")); // 第二次不再调 loader
        assertEquals(1, calls.get());

        // 批量
        Map<String, String> all = lc.getAll(java.util.Arrays.asList("2", "3"));
        assertEquals(2, all.size());
        assertEquals(3, calls.get());
        lc.shutdown();
    }
}
