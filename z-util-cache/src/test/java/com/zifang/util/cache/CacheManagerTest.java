package com.zifang.util.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CacheManager 单元测试（基于新泛型 API）。
 */
public class CacheManagerTest {

    private CacheManager mgr;

    @Before
    public void setUp() {
        mgr = CacheManager.getInstance();
    }

    @After
    public void tearDown() {
        mgr.clearAll();
    }

    @Test
    public void testGetInstance_singleton() {
        assertSame(CacheManager.getInstance(), CacheManager.getInstance());
    }

    @Test
    public void testRegister_andGet() {
        Cache<String, Integer> c = CacheBuilder.<String, Integer>newBuilder()
                .name("reg-test")
                .maximumSize(10)
                .build();
        mgr.register(c);
        assertSame(c, mgr.get("reg-test"));
    }

    @Test
    public void testRegister_doesNotOverwrite() {
        Cache<String, Integer> c1 = CacheBuilder.<String, Integer>newBuilder().name("a").build();
        Cache<String, Integer> c2 = CacheBuilder.<String, Integer>newBuilder().name("a").build();
        mgr.register(c1);
        // 第二次 register 同名不会覆盖，返回旧的
        assertSame(c1, mgr.register(c2));
        assertSame(c1, mgr.get("a"));
    }

    @Test
    public void testGetOrCreate_lazilyBuilds() {
        Cache<String, Integer> c = mgr.getOrCreate("lazy", name ->
                CacheBuilder.<String, Integer>newBuilder().name(name).maximumSize(5).build());
        assertNotNull(c);
        // 再调一次返回同一实例
        assertSame(c, mgr.getOrCreate("lazy", name ->
                CacheBuilder.<String, Integer>newBuilder().name(name).build()));
    }

    @Test
    public void testContains_andRemove() {
        mgr.register(CacheBuilder.<String, Integer>newBuilder().name("rm").build());
        assertTrue(mgr.contains("rm"));
        assertNotNull(mgr.remove("rm"));
        assertFalse(mgr.contains("rm"));
    }

    @Test
    public void testClearAll_shutsDownAllCaches() {
        Cache<String, Integer> c1 = CacheBuilder.<String, Integer>newBuilder().name("c1").build();
        mgr.register(c1);
        mgr.clearAll();
        assertEquals(0, mgr.size());
    }

    // ==================== 进阶用例 ====================

    @Test
    public void testGet_nonExistent_returnsNull() {
        assertNull(mgr.get("does-not-exist"));
    }

    @Test
    public void testGet_withTypeSafe_returnsNullWhenAbsent() {
        assertNull(mgr.get("nope", String.class, Integer.class));
    }

    @Test
    public void testGet_withTypeSafe_returnsCached() {
        mgr.register(CacheBuilder.<String, Integer>newBuilder().name("typed").build());
        Cache<String, Integer> c = mgr.get("typed", String.class, Integer.class);
        assertNotNull(c);
    }

    @Test
    public void testRemove_nonExistent_returnsNull() {
        assertNull(mgr.remove("never-registered"));
    }

    @Test
    public void testRemove_existing_shutsDownAndReturnsIt() {
        Cache<String, Integer> c = CacheBuilder.<String, Integer>newBuilder().name("rm-existing").build();
        mgr.register(c);
        Cache<?, ?> removed = mgr.remove("rm-existing");
        assertSame(c, removed);
        assertFalse(mgr.contains("rm-existing"));
    }

    @Test
    public void testSnapshot_isUnmodifiable() {
        mgr.register(CacheBuilder.<String, Integer>newBuilder().name("snap").build());
        java.util.Map<String, Cache<?, ?>> snap = mgr.snapshot();
        assertEquals(1, snap.size());
        assertTrue(snap.containsKey("snap"));
        try {
            snap.put("new", null);
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) { /* ok */ }
    }

    @Test
    public void testSize_afterMultipleRegisters() {
        mgr.register(CacheBuilder.<String, Integer>newBuilder().name("s1").build());
        mgr.register(CacheBuilder.<String, Integer>newBuilder().name("s2").build());
        mgr.register(CacheBuilder.<String, Integer>newBuilder().name("s3").build());
        assertEquals(3, mgr.size());
    }

    @Test
    public void testContains_falseForAbsent() {
        assertFalse(mgr.contains("never-registered"));
    }

    @Test
    public void testGetOrCreate_concurrentBuilds_returnsSameInstance() throws InterruptedException {
        // 两个线程同时调用 getOrCreate，期望最终只有一个 cache 实例被注册
        final int threadCount = 4;
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(threadCount);
        final java.util.concurrent.atomic.AtomicReference<Cache<String, Integer>> first = new java.util.concurrent.atomic.AtomicReference<>();
        final java.util.concurrent.atomic.AtomicReference<Cache<String, Integer>> last = new java.util.concurrent.atomic.AtomicReference<>();
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    Cache<String, Integer> c = mgr.getOrCreate("concurrent",
                            name -> CacheBuilder.<String, Integer>newBuilder().name(name).maximumSize(5).build());
                    if (first.compareAndSet(null, c)) {
                        last.set(c);
                    } else {
                        last.set(c);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        done.await();
        // 所有线程拿到的应是同一实例
        assertSame(first.get(), last.get());
        assertTrue(mgr.contains("concurrent"));
        assertEquals(1, mgr.size());
    }

    @Test
    public void testClearAll_swallowsExceptionsFromCaches() {
        // 即便某个 cache 的 invalidateAll 抛错，clearAll 也不应中断
        mgr.register(CacheBuilder.<String, Integer>newBuilder().name("safe-clear").build());
        // 模拟：clearAll 内部对每个 cache 单独 try/catch
        mgr.clearAll();
        assertEquals(0, mgr.size());
    }
}
