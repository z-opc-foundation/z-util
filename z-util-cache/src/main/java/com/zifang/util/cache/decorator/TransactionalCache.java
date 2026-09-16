package com.zifang.util.cache.decorator;

import com.zifang.util.cache.Cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 事务性装饰器：保证一组 put/remove 操作的原子性（在锁内批量执行）。
 * <p>
 * 用法：
 * <pre>{@code
 *   try (Transaction t = txCache.beginTransaction()) {
 *       t.put("a", 1);
 *       t.put("b", 2);
 *       t.remove("c");
 *       t.commit();   // 或 rollback
 *   }
 * }</pre>
 * <p>
 * <b>局限</b>：只是把 put/remove 序列化到 lock 段内；不提供跨 cache 的分布式事务。
 * 语义类似 Java {@code CopyOnWriteArrayList} 在多线程下的"看见一致性"。
 */
public class TransactionalCache<K, V> extends ForwardingCache<K, V> {

    private final Cache<K, V> delegate;
    private final ReentrantLock txLock = new ReentrantLock(true);

    public TransactionalCache(Cache<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Cache<K, V> delegate() {
        return delegate;
    }

    public Transaction beginTransaction() {
        txLock.lock();
        return new Transaction();
    }

    /**
     * 事务：所有 put/remove 都在一个锁段里完成。
     */
    public class Transaction implements AutoCloseable {
        private final Set<K> pendingRemovals = new HashSet<>();
        private final java.util.LinkedHashMap<K, V> pendingPuts = new java.util.LinkedHashMap<>();
        private boolean committed = false;
        private boolean rolledBack = false;

        public Transaction put(K key, V value) {
            pendingPuts.put(key, value);
            pendingRemovals.remove(key);
            return this;
        }

        public Transaction remove(K key) {
            pendingRemovals.add(key);
            pendingPuts.remove(key);
            return this;
        }

        public Transaction commit() {
            if (committed || rolledBack) throw new IllegalStateException("tx already finished");
            for (java.util.Map.Entry<K, V> e : pendingPuts.entrySet()) {
                delegate.put(e.getKey(), e.getValue());
            }
            for (K k : pendingRemovals) delegate.remove(k);
            committed = true;
            txLock.unlock();
            return this;
        }

        public Transaction rollback() {
            rolledBack = true;
            txLock.unlock();
            return this;
        }

        @Override
        public void close() {
            if (!committed && !rolledBack) rollback();
        }
    }
}
