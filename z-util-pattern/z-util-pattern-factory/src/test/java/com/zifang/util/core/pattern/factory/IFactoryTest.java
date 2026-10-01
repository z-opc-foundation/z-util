package com.zifang.util.core.pattern.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * IFactory 接口契约: 同 key 多次 getInstance 必须返同一对象 (典型缓存语义).
 * 这是工厂模式区别于单纯 provider 的关键 —— 调用方期待 instance 复用.
 */
class IFactoryTest {

    @Test
    @DisplayName("同 key 多次 getInstance 必须返同一实例 (cached/单例语义)")
    void sameKeyReturnsSameInstance() {
        IFactory<String, Object> f = new IFactory<String, Object>() {
            private Object cached;
            @Override
            public Object getInstance(String k) {
                if (cached == null) cached = new Object();
                return cached;
            }
        };
        Object a = f.getInstance("x");
        Object b = f.getInstance("x");
        assertSame(a, b, "工厂实现必须缓存, 这是与 OOP 单纯 new 的核心契约");
    }

    @Test
    @DisplayName("IFactory 本身是接口, 匿名实现必须能编译通过")
    void interfaceCompiles() {
        IFactory<Integer, String> f = k -> "v-" + k;
        assertEquals("v-1", f.getInstance(1));
        assertEquals("v-2", f.getInstance(2));
    }
}