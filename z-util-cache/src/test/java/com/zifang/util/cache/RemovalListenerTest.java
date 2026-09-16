package com.zifang.util.cache;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * RemovalListener / RemovalNotification / RemovalCause 单元测试。
 * <p>
 * 同时覆盖 {@link NullValue} 哨兵的单例与 toString。
 */
public class RemovalListenerTest {

    // ==================== RemovalNotification 字段 ====================

    @Test
    public void testRemovalNotification_getters() {
        RemovalListener.RemovalNotification<String, Integer> n =
                new RemovalListener.RemovalNotification<>("k", 100, RemovalListener.RemovalCause.EXPLICIT);
        assertEquals("k", n.getKey());
        assertEquals(Integer.valueOf(100), n.getValue());
        assertEquals(RemovalListener.RemovalCause.EXPLICIT, n.getCause());
    }

    @Test
    public void testRemovalNotification_nullKeyAndValueAllowed() {
        // 构造器不校验 null —— 与 listener 回调"刚刚被移除"的语义一致
        RemovalListener.RemovalNotification<String, String> n =
                new RemovalListener.RemovalNotification<>(null, null, RemovalListener.RemovalCause.EXPIRED);
        assertNull(n.getKey());
        assertNull(n.getValue());
        assertEquals(RemovalListener.RemovalCause.EXPIRED, n.getCause());
    }

    // ==================== RemovalCause 枚举 ====================

    @Test
    public void testRemovalCause_allValues() {
        RemovalListener.RemovalCause[] all = RemovalListener.RemovalCause.values();
        assertEquals(6, all.length);
        assertNotNull(RemovalListener.RemovalCause.valueOf("EXPLICIT"));
        assertNotNull(RemovalListener.RemovalCause.valueOf("REPLACED"));
        assertNotNull(RemovalListener.RemovalCause.valueOf("SIZE_LIMIT"));
        assertNotNull(RemovalListener.RemovalCause.valueOf("EXPIRED"));
        assertNotNull(RemovalListener.RemovalCause.valueOf("ACCESS_EXPIRED"));
        assertNotNull(RemovalListener.RemovalCause.valueOf("COLLECTED"));
    }

    // ==================== Listener 集成 ====================

    @Test
    public void testListener_invoked_withCause() {
        AtomicReference<RemovalListener.RemovalNotification<String, String>> captured = new AtomicReference<>();
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("listener-cause")
                .addListener(captured::set)
                .build();
        try {
            c.put("k", "v");
            c.remove("k");
            RemovalListener.RemovalNotification<String, String> n = captured.get();
            assertNotNull(n);
            assertEquals("k", n.getKey());
            assertEquals("v", n.getValue());
            assertEquals(RemovalListener.RemovalCause.EXPLICIT, n.getCause());
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testListener_collectedCause_onClear() {
        AtomicReference<RemovalListener.RemovalCause> cause = new AtomicReference<>();
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("listener-clear")
                .addListener(n -> cause.compareAndSet(null, n.getCause()))
                .build();
        try {
            c.put("a", "1");
            c.clear();
            assertEquals(RemovalListener.RemovalCause.COLLECTED, cause.get());
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testListener_exceptionDoesNotBreakMainFlow() {
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("listener-throws")
                .addListener(n -> {
                    throw new RuntimeException("listener boom");
                })
                .build();
        try {
            // 不应因为 listener 抛错而中断主流程
            c.put("k", "v");
            c.remove("k");    // EXPLICIT 移除，listener 抛错被吞
            // 缓存仍可继续使用
            assertNull(c.get("k"));
        } finally {
            c.shutdown();
        }
    }

    @Test
    public void testMultipleListeners_allInvoked() {
        final int[] counter = {0};
        Cache<String, String> c = CacheBuilder.<String, String>newBuilder()
                .name("multi-listener")
                .addListener(n -> counter[0]++)
                .addListener(n -> counter[0]++)
                .build();
        try {
            c.put("k", "v");
            c.remove("k");
            assertEquals(2, counter[0]);
        } finally {
            c.shutdown();
        }
    }

    // ==================== NullValue ====================

    @Test
    public void testNullValue_singleton() {
        assertSame(NullValue.INSTANCE, NullValue.INSTANCE);
        assertNotNull(NullValue.INSTANCE);
    }

    @Test
    public void testNullValue_toString() {
        assertEquals("NullValue", NullValue.INSTANCE.toString());
    }
}
