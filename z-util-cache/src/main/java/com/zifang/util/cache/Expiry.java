package com.zifang.util.cache;

import java.time.Duration;

/**
 * 自定义过期策略（对标 Caffeine 的 {@code Expiry}）。
 * <p>
 * 与 {@code expireAfterWrite}/{@code expireAfterAccess} 互斥；同时设置时优先 Expiry。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface Expiry<K, V> {

    /**
     * 永不过期的便利实现。
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    Expiry NEVER = new Expiry() {
        @Override
        public Duration expireAfterCreate(Object k, Object v) {
            return Duration.ofNanos(Long.MAX_VALUE);
        }

        @Override
        public Duration expireAfterUpdate(Object k, Object v, long t) {
            return Duration.ofNanos(Long.MAX_VALUE);
        }

        @Override
        public Duration expireAfterRead(Object k, Object v, long t) {
            return Duration.ofNanos(Long.MAX_VALUE);
        }
    };

    /**
     * 条目创建后多久过期。返回 {@link Duration#ZERO} 或负值表示永不过期。
     */
    Duration expireAfterCreate(K key, V value);

    /**
     * 条目最近一次读/写后多久过期。
     */
    Duration expireAfterUpdate(K key, V value, long currentTimeNanos);

    /**
     * 条目最近一次读后多久过期。
     */
    Duration expireAfterRead(K key, V value, long currentTimeNanos);
}
