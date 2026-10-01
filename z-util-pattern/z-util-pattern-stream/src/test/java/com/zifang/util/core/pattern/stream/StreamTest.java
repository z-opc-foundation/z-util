package com.zifang.util.core.pattern.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stream/Subscriber/Subscription/Emitter 契约层测试 —— 锁住 push 顺序与终止信号.
 */
class StreamTest {

    @Test
    @DisplayName("Emitter.from 推送 Iterable 全部元素, 顺序一致")
    void pushAllInOrder() {
        List<Integer> got = new ArrayList<Integer>();
        AtomicBoolean complete = new AtomicBoolean(false);
        Stream<Integer> s = Emitter.from(java.util.Arrays.asList(1, 2, 3));
        s.subscribe(new Subscriber<Integer>() {
            @Override public void onSubscribe(Subscription sub) { sub.request(Long.MAX_VALUE); }
            @Override public void onNext(Integer item) { got.add(item); }
            @Override public void onError(Throwable t) { }
            @Override public void onComplete() { complete.set(true); }
        });
        assertEquals(java.util.Arrays.asList(1, 2, 3), got, "Iterable 顺序必须保持");
        assertTrue(complete.get(), "正常结束必须 onComplete");
    }

    @Test
    @DisplayName("不显式 request(n) 时 Emitter 限速推 1 个, 防无脑全推")
    void backpressureDefaultIsOne() {
        final List<Integer> got = new ArrayList<Integer>();
        Stream<Integer> s = Emitter.from(java.util.Arrays.asList(1, 2, 3));
        s.subscribe(new Subscriber<Integer>() {
            @Override public void onSubscribe(Subscription sub) {
                // 不调 request: 只推 1 个
            }
            @Override public void onNext(Integer item) { got.add(item); }
            @Override public void onError(Throwable t) { }
            @Override public void onComplete() { }
        });
        assertEquals(1, got.size(),
                "不显式 request 必须只推 1 个, 防无脑全推撞爆下游 (调方想全量应 request(Long.MAX_VALUE))");
    }

    @Test
    @DisplayName("cancel 之后 onNext/onComplete 不再被调用")
    void cancelStopsPush() {
        final java.util.concurrent.atomic.AtomicReference<Subscription> subRef =
                new java.util.concurrent.atomic.AtomicReference<Subscription>();
        final java.util.concurrent.atomic.AtomicBoolean completeCalled = new java.util.concurrent.atomic.AtomicBoolean(false);
        Emitter.from(java.util.Arrays.asList(1, 2, 3, 4, 5)).subscribe(new Subscriber<Integer>() {
            @Override public void onSubscribe(Subscription sub) {
                subRef.set(sub);
                sub.request(2);
            }
            @Override public void onNext(Integer item) {
                if (item != null && item == 2) {
                    subRef.get().cancel();
                }
            }
            @Override public void onError(Throwable t) { }
            @Override public void onComplete() { completeCalled.set(true); }
        });
        assertFalse(completeCalled.get(), "cancel 之后 onComplete 不应再被调用");
    }

    @Test
    @DisplayName("Emitter.from(null) 立即抛 IllegalArgumentException")
    void nullSourceRejected() {
        assertThrows(IllegalArgumentException.class, () -> Emitter.from(null));
    }

    @Test
    @DisplayName("Emitter.of(...) 等价 from(Arrays.asList(...))")
    void ofVarargsEqualsFrom() {
        List<String> got = new ArrayList<String>();
        Emitter.of("a", "b", "c").subscribe(new Subscriber<String>() {
            @Override public void onSubscribe(Subscription sub) { sub.request(Long.MAX_VALUE); }
            @Override public void onNext(String item) { got.add(item); }
            @Override public void onError(Throwable t) { }
            @Override public void onComplete() { }
        });
        assertEquals(java.util.Arrays.asList("a", "b", "c"), got);
    }
}