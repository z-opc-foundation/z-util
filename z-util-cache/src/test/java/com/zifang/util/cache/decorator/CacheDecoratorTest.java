package com.zifang.util.cache.decorator;

import com.zifang.util.cache.Cache;
import com.zifang.util.cache.CacheBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * cache 装饰器测试。
 */
public class CacheDecoratorTest {

    private Cache<String, String> base() {
        return CacheBuilder.<String, String>newBuilder()
                .name("test")
                .maximumSize(100)
                .recordStats()
                .build();
    }

    @Test
    public void testMeteredCache() {
        Cache<String, String> c = new MeteredCache<>(base());
        c.put("a", "1");
        c.get("a");     // hit
        c.get("z");     // miss
        MeteredCache<String, String> m = (MeteredCache<String, String>) c;
        assertEquals(1, m.meter().hitCount());
        assertEquals(1, m.meter().missCount());
        assertEquals(1, m.meter().putCount());
        assertTrue("hitRate should be ~0.5, got " + m.meter().hitRate(), Math.abs(m.meter().hitRate() - 0.5) < 0.01);
    }

    @Test
    public void testBoundedCache_dropsWhenFull() {
        BoundedCache<String, String> bc = new BoundedCache<>(base(), 3);
        bc.put("a", "1");
        bc.put("b", "2");
        bc.put("c", "3");
        bc.put("d", "4");   // 满了
        assertEquals(3, bc.currentSize());
        assertNull(bc.get("d"));
    }

    @Test
    public void testBoundedCache_tryPutReturnsBool() {
        BoundedCache<String, String> bc = new BoundedCache<>(base(), 2);
        assertTrue(bc.tryPut("a", "1"));
        assertTrue(bc.tryPut("b", "2"));
        assertFalse(bc.tryPut("c", "3"));
    }

    @Test
    public void testTransactionalCache_commit() {
        TransactionalCache<String, String> tx = new TransactionalCache<>(base());
        try (TransactionalCache<String, String>.Transaction t = tx.beginTransaction()) {
            t.put("a", "1");
            t.put("b", "2");
            t.remove("x");
            t.commit();
        }
        assertEquals("1", tx.get("a"));
        assertEquals("2", tx.get("b"));
    }

    @Test
    public void testTransactionalCache_rollback() {
        TransactionalCache<String, String> tx = new TransactionalCache<>(base());
        try (TransactionalCache<String, String>.Transaction t = tx.beginTransaction()) {
            t.put("a", "1");
            t.put("b", "2");
            t.rollback();
        }
        assertNull(tx.get("a"));
        assertNull(tx.get("b"));
    }

    @Test
    public void testNullCache_alwaysReturnsNull() {
        Cache<String, String> c = new NullCache<>();
        c.put("a", "1");
        assertNull(c.get("a"));
        assertFalse(c.contains("a"));
        assertEquals(0L, c.size());
        assertEquals("null-cache", c.getName());
    }

    // ==================== MeteredCache 进阶 ====================

    @Test
    public void testMeteredCache_loaderHitAndMiss() {
        MeteredCache<String, String> c = new MeteredCache<>(base());
        // hit: loader 返回非 null → cache.get(k, loader) 返回值非 null
        c.get("a", k -> "1");
        // miss: loader 返回 null
        c.get("b", k -> null);
        assertEquals(1L, c.meter().hitCount());
        assertEquals(1L, c.meter().missCount());
    }

    @Test
    public void testMeteredCache_loaderException_countsAsMiss() {
        MeteredCache<String, String> c = new MeteredCache<>(base());
        try {
            c.get("k", k -> {
                throw new IllegalStateException("loader-boom");
            });
            fail("expected");
        } catch (IllegalStateException expected) { /* ok */ }
        assertEquals(1L, c.meter().missCount());
        assertEquals(0L, c.meter().hitCount());
    }

    @Test
    public void testMeteredCache_remove_returnsFalseNotCounted() {
        MeteredCache<String, String> c = new MeteredCache<>(base());
        // 移除不存在的 key → 不计数
        boolean removed = c.remove("absent");
        assertFalse(removed);
        assertEquals(0L, c.meter().removeCount());
    }

    @Test
    public void testMeteredCache_remove_existing_incrementsCount() {
        MeteredCache<String, String> c = new MeteredCache<>(base());
        c.put("k", "v");
        c.remove("k");
        assertEquals(1L, c.meter().removeCount());
    }

    @Test
    public void testMeteredCache_clear_incrementsEvictionsByClearedCount() {
        MeteredCache<String, String> c = new MeteredCache<>(base());
        c.put("a", "1");
        c.put("b", "2");
        c.put("c", "3");
        c.clear();
        // clear 时记录了 3 个 evict
        assertEquals(3L, c.meter().evictionCount());
    }

    @Test
    public void testMeteredCache_emptyHasZeroHitRate() {
        MeteredCache<String, String> c = new MeteredCache<>(base());
        // 没有任何 get 时，hitRate = 0
        assertEquals(0.0, c.meter().hitRate(), 0.0001);
    }

    @Test
    public void testMeteredCache_getCount_sumsHitAndMiss() {
        MeteredCache<String, String> c = new MeteredCache<>(base());
        c.put("a", "1");
        c.get("a"); // hit
        c.get("z"); // miss
        c.get("y"); // miss
        assertEquals(3L, c.meter().getCount());
    }

    // ==================== BoundedCache 进阶 ====================

    @Test(expected = IllegalArgumentException.class)
    public void testBoundedCache_invalidMaxSizeThrows() {
        new BoundedCache<>(base(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBoundedCache_negativeMaxSizeThrows() {
        new BoundedCache<>(base(), -1);
    }

    @Test
    public void testBoundedCache_updateExisting_doesNotCountAsNew() {
        // 已存在的 key 更新值，不应被当作"新增"而被丢弃
        BoundedCache<String, String> bc = new BoundedCache<>(base(), 2);
        assertTrue(bc.tryPut("a", "1"));
        assertTrue(bc.tryPut("b", "2"));
        // 已满但 key 已存在 → 允许更新
        assertTrue(bc.tryPut("a", "1-updated"));
        assertEquals("1-updated", bc.get("a"));
    }

    @Test
    public void testBoundedCache_maxSizeAccessor() {
        BoundedCache<String, String> bc = new BoundedCache<>(base(), 5);
        assertEquals(5L, bc.maxSize());
    }

    @Test
    public void testBoundedCache_remove_updatesCurrentSize() {
        BoundedCache<String, String> bc = new BoundedCache<>(base(), 5);
        bc.put("a", "1");
        bc.put("b", "2");
        assertEquals(2L, bc.currentSize());
        bc.remove("a");
        assertEquals(1L, bc.currentSize());
    }

    // ==================== TransactionalCache 进阶 ====================

    @Test
    public void testTransactionalCache_commitTwiceThrows() {
        TransactionalCache<String, String> tx = new TransactionalCache<>(base());
        try (TransactionalCache<String, String>.Transaction t = tx.beginTransaction()) {
            t.put("a", "1");
            t.commit();
            try {
                t.commit();
                fail("expected IllegalStateException");
            } catch (IllegalStateException expected) { /* ok */ }
        }
    }

    @Test
    public void testTransactionalCache_doubleRollbackThrows() {
        // 同一事务 rollback 两次：第二次会因锁未持有而抛 IllegalMonitorStateException
        TransactionalCache<String, String> tx = new TransactionalCache<>(base());
        try (TransactionalCache<String, String>.Transaction t = tx.beginTransaction()) {
            t.put("a", "1");
            t.rollback();
            try {
                t.rollback();
                fail("expected exception");
            } catch (IllegalMonitorStateException expected) { /* ok */ }
        }
    }

    @Test
    public void testTransactionalCache_autoRollbackOnClose() {
        // try-with-resources 不显式 commit，close 时应自动 rollback
        TransactionalCache<String, String> tx = new TransactionalCache<>(base());
        try (TransactionalCache<String, String>.Transaction t = tx.beginTransaction()) {
            t.put("a", "1");
            // 不 commit 也不 rollback，依赖 close
        }
        assertNull(tx.get("a"));
    }

    @Test
    public void testTransactionalCache_putThenRemoveSameKey_inSameTx() {
        // put 后 remove → 提交时不应保留
        TransactionalCache<String, String> tx = new TransactionalCache<>(base());
        try (TransactionalCache<String, String>.Transaction t = tx.beginTransaction()) {
            t.put("a", "1").remove("a").commit();
        }
        assertNull(tx.get("a"));
    }

    @Test
    public void testTransactionalCache_removeThenPutSameKey_inSameTx() {
        // remove 后 put → 提交时应保留（put 后置生效）
        TransactionalCache<String, String> tx = new TransactionalCache<>(base());
        try (TransactionalCache<String, String>.Transaction t = tx.beginTransaction()) {
            t.remove("a").put("a", "1").commit();
        }
        assertEquals("1", tx.get("a"));
    }

    // ==================== NullCache 进阶 ====================

    @Test
    public void testNullCache_getWithLoader_callsLoaderAndReturnsValue() {
        // NullCache 的 get(k, loader) 直接调 loader（绕过"无缓存"语义）
        Cache<String, String> c = new NullCache<>();
        String v = c.get("k", k -> "loaded-" + k);
        assertEquals("loaded-k", v);
    }

    @Test
    public void testNullCache_getWithNullLoader_returnsNull() {
        Cache<String, String> c = new NullCache<>();
        assertNull(c.get("k", null));
    }

    @Test
    public void testNullCache_putWithLoader_isNoop() {
        Cache<String, String> c = new NullCache<>();
        c.put("k", (java.util.function.Function<String, String>) k -> "v");
        assertNull(c.get("k"));
        assertEquals(0L, c.size());
    }

    @Test
    public void testNullCache_getAllPresent_returnsEmpty() {
        Cache<String, String> c = new NullCache<>();
        java.util.Map<String, String> got = c.getAllPresent(java.util.Arrays.asList("a", "b"));
        assertTrue(got.isEmpty());
    }

    @Test
    public void testNullCache_asMap_returnsEmpty() {
        Cache<String, String> c = new NullCache<>();
        assertTrue(c.asMap().isEmpty());
    }

    @Test
    public void testNullCache_stats_returnsNull() {
        Cache<String, String> c = new NullCache<>();
        assertNull(c.stats());
    }

    @Test
    public void testNullCache_removalListeners_returnsEmpty() {
        Cache<String, String> c = new NullCache<>();
        assertTrue(c.removalListeners().isEmpty());
    }

    @Test
    public void testNullCache_shutdown_isNoop() {
        // shutdown 不应抛错
        Cache<String, String> c = new NullCache<>();
        c.shutdown();
    }

    // ==================== ForwardingCache 转发一致性 ====================

    @Test
    public void testForwardingCache_forwardsAllMethods() {
        // 自定义 ForwardingCache 子类，仅暴露 delegate，验证所有方法都被正确转发
        final Cache<String, String>[] holder = new Cache[]{base()};
        ForwardingCache<String, String> forwarding = new ForwardingCache<String, String>() {
            @Override
            protected Cache<String, String> delegate() {
                return holder[0];
            }
        };
        forwarding.put("k", "v");
        assertEquals("v", forwarding.get("k"));
        assertTrue(forwarding.contains("k"));
        assertEquals(1L, forwarding.size());
        assertEquals("v", forwarding.asMap().get("k"));
        forwarding.invalidateAll();
        assertEquals(0L, forwarding.size());
        forwarding.shutdown();
    }
}
