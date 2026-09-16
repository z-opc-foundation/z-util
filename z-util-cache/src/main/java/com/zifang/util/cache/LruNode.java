package com.zifang.util.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自研 LRU 区段（对标 Caffeine 的 Window 区段或 LRU 段）。
 * <p>
 * 用 {@link LinkedHashMap#accessOrder} 实现 LRU：访问过的 key 会自动移到队尾，淘汰时取队首。
 * <p>
 * 线程安全：所有方法需在外部锁内调用（与 {@link WTinyLFU} 共用同一把锁）。
 */
final class LruNode<K, V> {

    /**
     * 访问顺序 = true；3 = initialCapacity, 0.75f = loadFactor, true = accessOrder
     */
    private final LinkedHashMap<K, Entry<K, V>> map = new LinkedHashMap<>(16, 0.75f, true);
    /**
     * 区段内近似元素数（用 AtomicLong 是为了 stats 读取时免锁）。
     */
    private final AtomicLong size = new AtomicLong();
    /**
     * 当前区段容量。
     */
    private int maxCapacity;

    LruNode(int initialCapacity) {
        this.maxCapacity = Math.max(1, initialCapacity);
    }

    V get(K key) {
        Entry<K, V> e = map.get(key);
        return e == null ? null : e.value;
    }

    /**
     * 是否包含。
     */
    boolean contains(K key) {
        return map.containsKey(key);
    }

    /**
     * 放入。若已存在则覆盖。返回被替换的旧值（可能为 null）。
     */
    V put(K key, V value) {
        Entry<K, V> old = map.put(key, new Entry<>(key, value));
        if (old == null) {
            size.incrementAndGet();
        }
        return old == null ? null : old.value;
    }

    /**
     * 移除指定 key。
     */
    V remove(K key) {
        Entry<K, V> e = map.remove(key);
        if (e != null) {
            size.decrementAndGet();
            return e.value;
        }
        return null;
    }

    /**
     * 弹出 LRU 端（队首）。
     */
    Map.Entry<K, V> evict() {
        Iterator<Map.Entry<K, Entry<K, V>>> it = map.entrySet().iterator();
        if (!it.hasNext()) return null;
        Map.Entry<K, Entry<K, V>> e = it.next();
        it.remove();
        size.decrementAndGet();
        return new java.util.AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().value);
    }

    int size() {
        return (int) Math.min(size.get(), Integer.MAX_VALUE);
    }

    int maxCapacity() {
        return maxCapacity;
    }

    /**
     * 查看 LRU 队首（不弹出）。
     */
    Map.Entry<K, V> peekFirst() {
        Iterator<Map.Entry<K, Entry<K, V>>> it = map.entrySet().iterator();
        if (!it.hasNext()) return null;
        Map.Entry<K, Entry<K, V>> e = it.next();
        return new java.util.AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().value);
    }

    void setMaxCapacity(int c) {
        this.maxCapacity = Math.max(1, c);
    }

    boolean isEmpty() {
        return size() == 0;
    }

    static final class Entry<K, V> {
        final K key;
        final V value;

        Entry(K k, V v) {
            this.key = k;
            this.value = v;
        }
    }
}
