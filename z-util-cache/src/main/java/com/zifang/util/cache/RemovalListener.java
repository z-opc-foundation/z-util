package com.zifang.util.cache;

/**
 * 缓存条目被移除时的回调监听器。
 * <p>
 * 触发时机：put 覆盖旧值、remove 主动删除、容量超限淘汰、TTL 过期。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
@FunctionalInterface
public interface RemovalListener<K, V> {

    /**
     * 当一个条目被移除时调用。抛出异常会被吞掉（监听器不影响主流程）。
     */
    void onRemoval(RemovalNotification<K, V> notification);

    /**
     * 移除原因。
     */
    enum RemovalCause {
        /**
         * 显式调用 {@link Cache#remove(Object)}
         */
        EXPLICIT,
        /**
         * 被新值覆盖
         */
        REPLACED,
        /**
         * 容量超限被淘汰
         */
        SIZE_LIMIT,
        /**
         * 写入后存活时间过期
         */
        EXPIRED,
        /**
         * 最后一次访问后空闲时间过期
         */
        ACCESS_EXPIRED,
        /**
         * 缓存被整体清空
         */
        COLLECTED
    }

    /**
     * 移除事件详情。
     */
    final class RemovalNotification<K, V> {
        private final K key;
        private final V value;
        private final RemovalCause cause;

        public RemovalNotification(K key, V value, RemovalCause cause) {
            this.key = key;
            this.value = value;
            this.cause = cause;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public RemovalCause getCause() {
            return cause;
        }
    }
}
