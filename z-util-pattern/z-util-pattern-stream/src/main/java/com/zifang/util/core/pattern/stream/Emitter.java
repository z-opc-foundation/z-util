package com.zifang.util.core.pattern.stream;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 流创建工具: 把 Iterable/数组包成 Stream; 简单的同步 push 实现, 不带线程.
 * <p>
 * 异步/多线程消费场景另写 (e.g. ChannelStream), 本类只覆盖最常见的 "已有数据 -> push".
 */
public final class Emitter {

    private Emitter() {}

    /**
     * 从 Iterable 构造: 订阅时同步遍历, 按 Subscriber 的 request(n) 限速 push.
     */
    public static <T> Stream<T> from(final Iterable<T> source) {
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        return new IterableStream<T>(source);
    }

    /** 数组快捷. */
    @SafeVarargs
    public static <T> Stream<T> of(final T... items) {
        return from(new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                java.util.List<T> l = new java.util.ArrayList<T>(items.length);
                for (T t : items) {
                    l.add(t);
                }
                return l.iterator();
            }
        });
    }

    private static final class IterableStream<T> implements Stream<T> {
        private final Iterable<T> source;
        IterableStream(Iterable<T> source) { this.source = source; }

        @Override
        public void subscribe(final Subscriber<T> s) {
            if (s == null) {
                throw new IllegalArgumentException("subscriber is null");
            }
            final AtomicReference<Subscription> subRef = new AtomicReference<Subscription>();
            final AtomicLong requested = new AtomicLong(0);
            Subscription sub = new Subscription() {
                @Override public void request(long n) {
                    if (n <= 0) n = Long.MAX_VALUE;
                    requested.addAndGet(n);
                }
                @Override public void cancel() {
                    Subscription prev = subRef.getAndSet(SENTINEL);
                    if (prev != SENTINEL) {
                        requested.set(0);
                    }
                }
                @Override public boolean isCancelled() {
                    return subRef.get() == SENTINEL;
                }
            };
            subRef.set(sub);
            s.onSubscribe(sub);
            try {
                boolean defaultedOnce = false;
                for (T item : source) {
                    if (subRef.get() == SENTINEL) return;
                    long r = requested.get();
                    if (r == Long.MAX_VALUE) {
                        s.onNext(item);
                    } else if (r > 0) {
                        requested.decrementAndGet();
                        s.onNext(item);
                    } else {
                        // 没有任何 request: 只兜底推 1 个, 第二次直接 break.
                        // 调用方想全量就显式 request(Long.MAX_VALUE).
                        if (defaultedOnce) break;
                        defaultedOnce = true;
                        s.onNext(item);
                    }
                }
                if (subRef.get() != SENTINEL) {
                    s.onComplete();
                }
            } catch (Throwable t) {
                if (subRef.get() != SENTINEL) {
                    s.onError(t);
                }
            }
        }
    }

    private static final Subscription SENTINEL = new Subscription() {
        @Override public void request(long n) { }
        @Override public void cancel() { }
        @Override public boolean isCancelled() { return true; }
    };
}