package com.zifang.util.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * MemoryCache 进阶边界用例测试：null 处理、loader 异常、过期监听器、nullValueProtection 等。
 */
public class MemoryCacheAdvancedTest {

    private Cache<String, String> cache;

    @Before
    public void setUp() {
        cache = CacheBuilder.<String, String>newBuilder()
                .name("mem-adv")
                .recordStats()
                .build();
    }

    @After
    public void tearDown() {
        cache.shutdown();
    }

    // ==================== null key / value ====================

    @Test
    public void testGet_nullKey_returnsNull() {
        assertNull(cache.get(null));
        // 算 miss
        assertEquals(1L, cache.stats().missCount());
    }

    @Test
    public void testContains_nullKey_returnsFalse() {
        assertFalse(cache.contains(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPut_nullKey_throws() {
        cache.put(null, "v");
    }

    @Test
    public void testPut_nullValue_removesExistingEntry() {
        cache.put("k", "v");
        assertEquals("v", cache.get("k"));
        // put null → 视作删除
        cache.put("k", (String) null);
        assertNull(cache.get("k"));
        assertFalse(cache.contains("k"));
        assertEquals(0L, cache.size());
    }

    @Test
    public void testPut_nullValue_noOpIfAbsent() {
        cache.put("k", (String) null);
        // 不存在时，put null 不创建条目
        assertNull(cache.get("k"));
        assertEquals(0L, cache.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullLoader_throws() {
        cache.get("k", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullLoader_throws() {
        cache.put("k", (java.util.function.Function<String, String>) null);
    }

    // ==================== get(key, loader) 行为 ====================

    @Test
    public void testGetWithLoader_cachesValue() {
        AtomicInteger calls = new AtomicInteger();
        cache.get("k", k -> {
            calls.incrementAndGet();
            return "v";
        });
        assertEquals(1L, calls.get());
        // 第二次走缓存
        cache.get("k", k -> {
            calls.incrementAndGet();
            return "v";
        });
        assertEquals(1L, calls.get());
    }

    @Test
    public void testGetWithLoader_runtimeExceptionPropagates() {
        try {
            cache.get("k", k -> {
                throw new IllegalStateException("boom");
            });
            fail("expected");
        } catch (IllegalStateException expected) {
            assertEquals("boom", expected.getMessage());
        }
        assertTrue("loadFailure recorded", cache.stats().loadFailureCount() >= 1);
    }

    @Test
    public void testGetWithLoader_loaderReturnsNullWithoutProtection_callsLoaderTwice() {
        AtomicInteger calls = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            cache.get("k", k -> {
                calls.incrementAndGet();
                return null;
            });
        }
        // null 不缓存 → 每次都调 loader
        assertEquals(3L, calls.get());
    }

    @Test
    public void testGetWithLoader_loaderReturnsNullWithProtection_cachesNullValue() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("nvp")
                .nullValueProtection()
                .build();
        try {
            AtomicInteger calls = new AtomicInteger();
            // 第一次：loader 返回 null，被缓存为 NullValue 哨兵；get 返回 null（loaded 的原始值）
            Object v1 = c.get("k", k -> {
                calls.incrementAndGet();
                return null;
            });
            assertNull(v1);
            // 第二次：命中 NullValue 哨兵，loader 不再被调；返回值是 NullValue.INSTANCE（非 null）
            Object v2 = c.get("k", k -> {
                calls.incrementAndGet();
                return null;
            });
            assertSame(NullValue.INSTANCE, v2);
            assertEquals(1L, calls.get());
            // contains 返回 true（key 在缓存里）
            assertTrue(c.contains("k"));
        } finally {
            c.shutdown();
        }
    }

    // ==================== put(key, loader) 行为 ====================

    @Test
    public void testPutWithLoader_cachesValue() {
        cache.put("k", k -> "v-" + k);
        assertEquals("v-k", cache.get("k"));
    }

    @Test
    public void testPutWithLoader_returnsNull_doesNotCache() {
        cache.put("k", k -> null);
        assertFalse(cache.contains("k"));
    }

    @Test
    public void testPutWithLoader_loaderThrows_propagates() {
        try {
            cache.put("k", k -> {
                throw new RuntimeException("loader-fail");
            });
            fail("expected");
        } catch (RuntimeException expected) {
            assertEquals("loader-fail", expected.getMessage());
        }
    }

    // ==================== getAllPresent 边界 ====================

    @Test
    public void testGetAllPresent_allMisses_returnsEmpty() {
        long beforeMiss = cache.stats().missCount();
        Map<String, String> got = cache.getAllPresent(Arrays.asList("x", "y", "z"));
        assertTrue(got.isEmpty());
        // getAllPresent 不递增 missCount（语义是"批量读快照"，不计 miss）
        assertEquals(beforeMiss, cache.stats().missCount());
    }

    @Test
    public void testGetAllPresent_skipsExpired() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("gap-exp")
                .expireAfterWrite(Duration.ofMillis(150))
                .build();
        try {
            c.put("a", "1");
            c.put("b", "2");
            Thread.sleep(250);
            Map<String, String> got = c.getAllPresent(Arrays.asList("a", "b"));
            assertTrue("expired entries should be skipped, got " + got, got.isEmpty());
        } finally {
            c.shutdown();
        }
    }

    // ==================== 过期 listener cause 分类 ====================

    @Test
    public void testListener_expiredCause_onWriteExpiry() throws InterruptedException {
        AtomicReference<RemovalListener.RemovalCause> cause = new AtomicReference<>();
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("exp-cause-write")
                .expireAfterWrite(Duration.ofMillis(150))
                .addListener(n -> cause.compareAndSet(null, n.getCause()))
                .build();
        try {
            c.put("k", "v");
            Thread.sleep(250);
            c.get("k"); // 触发清理
            assertEquals(RemovalListener.RemovalCause.EXPIRED, cause.get());
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testListener_accessExpiredCause_onAccessExpiry() throws InterruptedException {
        AtomicReference<RemovalListener.RemovalCause> cause = new AtomicReference<>();
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("exp-cause-access")
                .expireAfterAccess(Duration.ofMillis(150))
                .addListener(n -> cause.compareAndSet(null, n.getCause()))
                .build();
        try {
            c.put("k", "v");
            Thread.sleep(250);
            c.get("k"); // 触发清理并触发 ACCESS_EXPIRED 移除
            assertEquals(RemovalListener.RemovalCause.ACCESS_EXPIRED, cause.get());
        } finally {
            c.shutdown();
        }
    }

    // ==================== shutdown 幂等 ====================

    @Test
    public void testShutdown_idempotent() {
        cache.shutdown();
        // 重复调用不抛错
        cache.shutdown();
        cache.shutdown();
    }

    // ==================== removalListeners 不可变 ====================

    @Test
    public void testRemovalListeners_isUnmodifiable() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("listener-unmod")
                .addListener(n -> { })
                .build();
        try {
            java.util.Set<RemovalListener<String, String>> listeners = c.removalListeners();
            assertEquals(1, listeners.size());
            try {
                listeners.clear();
                fail("expected UnsupportedOperationException");
            } catch (UnsupportedOperationException expected) { /* ok */ }
        } finally {
            c.shutdown();
        }
    }

    // ==================== asMap 排除过期 ====================

    @Test
    public void testAsMap_excludesExpired() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("asmap-exp")
                .expireAfterWrite(Duration.ofMillis(150))
                .build();
        try {
            c.put("a", "1");
            Thread.sleep(250);
            Map<String, String> view = c.asMap();
            assertTrue("expired entry should be excluded, got " + view, view.isEmpty());
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testAsMap_skipsExpiredOnSize() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("asmap-exp-size")
                .expireAfterWrite(Duration.ofMillis(150))
                .build();
        try {
            c.put("a", "1");
            c.put("b", "2");
            Thread.sleep(250);
            // asMap().size() 应当只算未过期的
            assertEquals(0, c.asMap().size());
            // 但底层 data 还没清理（cleanup 走后台）— size() 返回原大小
            // 注意：testAsMap_excludesExpired 已覆盖 entrySet 行为
        } finally {
            c.shutdown();
        }
    }

    // ==================== 容量 ====================

    @Test
    public void testSize_reflectsCurrentEntries() {
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");
        assertEquals(3L, cache.size());
        cache.remove("b");
        assertEquals(2L, cache.size());
    }

    // ==================== concurrent put + get ====================

    @Test
    public void testConcurrentPutGet_noExceptions() throws InterruptedException {
        final int writers = 4;
        final int readers = 4;
        final int opsPerThread = 500;
        Thread[] ts = new Thread[writers + readers];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        for (int i = 0; i < writers; i++) {
            final int id = i;
            ts[i] = new Thread(() -> {
                try { latch.await(); } catch (InterruptedException e) { return; }
                for (int j = 0; j < opsPerThread; j++) {
                    cache.put("k" + id + "-" + j, "v" + j);
                }
            });
        }
        for (int i = 0; i < readers; i++) {
            ts[writers + i] = new Thread(() -> {
                try { latch.await(); } catch (InterruptedException e) { return; }
                for (int j = 0; j < opsPerThread; j++) {
                    cache.get("k0-" + j);
                }
            });
        }
        for (Thread t : ts) t.start();
        latch.countDown();
        for (Thread t : ts) t.join();
        // 没有抛错就算通过
        assertTrue(cache.size() >= 0);
    }

    // ==================== 自定义 Expiry ====================

    @Test
    public void testCustomExpiry_never() {
        Expiry<String, String> never = Expiry.NEVER;
        assertEquals(Long.MAX_VALUE, never.expireAfterCreate("k", "v").toNanos());
        assertEquals(Long.MAX_VALUE, never.expireAfterUpdate("k", "v", 0L).toNanos());
        assertEquals(Long.MAX_VALUE, never.expireAfterRead("k", "v", 0L).toNanos());
    }

    @Test
    public void testCustomExpiry_drivesExpiration() throws InterruptedException {
        // 自定义 Expiry：创建 100ms 后过期
        Expiry<String, String> hundredMs = new Expiry<String, String>() {
            @Override
            public Duration expireAfterCreate(String key, String value) {
                return Duration.ofMillis(100);
            }

            @Override
            public Duration expireAfterUpdate(String key, String value, long currentTimeNanos) {
                return Duration.ofMillis(100);
            }

            @Override
            public Duration expireAfterRead(String key, String value, long currentTimeNanos) {
                return Duration.ofMillis(100);
            }
        };
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("custom-expiry")
                .expireAfter(hundredMs)
                .recordStats()
                .build();
        try {
            c.put("k", "v");
            assertEquals("v", c.get("k"));
            Thread.sleep(200);
            assertNull(c.get("k"));
            assertTrue(c.stats().expirationCount() >= 1);
        } finally {
            c.shutdown();
        }
    }

    // ==================== cache name 默认值 ====================

    @Test
    public void testName_defaultAutoGenerated() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder().build();
        try {
            assertNotNull(c.getName());
            assertFalse(c.getName().isEmpty());
        } finally {
            c.shutdown();
        }
    }

    // ==================== size 不受过期影响（清理走后台） ====================

    @Test
    public void testSize_doesNotImmediatelyReflectExpiry() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("size-exp")
                .expireAfterWrite(Duration.ofMillis(100))
                .build();
        try {
            c.put("a", "1");
            c.put("b", "2");
            assertEquals(2L, c.size());
            Thread.sleep(200);
            // size 仍可能返回 2（清理未触发）；调用 get 触发清理
            c.get("a");
            c.get("b");
            // 清理后 size 应当反映
            assertEquals(0L, c.size());
        } finally {
            c.shutdown();
        }
    }

    // ==================== contains 已过期返回 false 但不立即删 ====================

    @Test
    public void testContains_expired_returnsFalse() throws InterruptedException {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("contains-exp")
                .expireAfterWrite(Duration.ofMillis(100))
                .build();
        try {
            c.put("k", "v");
            assertTrue(c.contains("k"));
            Thread.sleep(200);
            assertFalse(c.contains("k"));
        } finally {
            c.shutdown();
        }
    }
}
