package com.zifang.util.core.pattern.pool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于栈的带键对象池实现
 *
 * @param <K> 键类型
 * @param <V> 对象类型
 */
public class StackKeyedObjectPool<K, V> implements KeyedObjectPool<K, V> {

    private final PooledObjectFactory<V> factory;
    private final PoolConfig defaultConfig;

    private final Map<K, PooledObjectStack<V>> pools;
    private final Map<K, Map<V, PooledObject<V>>> allocatedObjects;
    private final Map<K, AtomicInteger> activeCount;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private volatile boolean closed;

    /**
     * StackKeyedObjectPool方法。
     * * @param factory PooledObjectFactoryV类型参数
     */
    public StackKeyedObjectPool(PooledObjectFactory<V> factory) {
        this(factory, new PoolConfig());
    }

    /**
     * StackKeyedObjectPool方法。
     * * @param factory PooledObjectFactoryV类型参数
     *
     * @param defaultConfig PoolConfig类型参数
     */
    public StackKeyedObjectPool(PooledObjectFactory<V> factory, PoolConfig defaultConfig) {
        this.factory = factory;
        this.defaultConfig = defaultConfig;
        this.pools = new ConcurrentHashMap<>();
        this.allocatedObjects = new ConcurrentHashMap<>();
        this.activeCount = new ConcurrentHashMap<>();
        this.closed = false;
    }

    @Override
    /**
     * borrowObject方法。
     *      * @param key K类型参数
     * @return V类型返回值
     */
    public V borrowObject(K key) throws Exception {
        checkOpen();

        PooledObjectStack<V> stack = pools.computeIfAbsent(key, k -> new PooledObjectStack<>());
        AtomicInteger active = activeCount.computeIfAbsent(key, k -> new AtomicInteger(0));

        V object = null;
        PooledObject<V> pooledObject = null;

        lock.lock();
        try {
            while (object == null) {
                if (closed) {
                    throw new IllegalStateException("Pool is closed");
                }

                pooledObject = stack.pop();
                if (pooledObject != null) {
                    if (defaultConfig.isTestOnBorrow() && !factory.validateObject(pooledObject)) {
                        destroyObject(key, pooledObject);
                        continue;
                    }

                    if (pooledObject.allocate()) {
                        factory.activateObject(pooledObject);
                        object = pooledObject.getObject();
                        pooledObject.recordBorrow();
                        active.incrementAndGet();
                        allocatedObjects.computeIfAbsent(key, k -> new HashMap<>()).put(object, pooledObject);
                        return object;
                    }
                }

                if (active.get() < defaultConfig.getMaxTotal()) {
                    pooledObject = factory.makeObject();
                    if (pooledObject != null) {
                        pooledObject.allocate();
                        // 验证新创建的对象
                        if (defaultConfig.isTestOnBorrow() && !factory.validateObject(pooledObject)) {
                            destroyObject(key, pooledObject);
                            pooledObject = null;
                            continue;
                        }
                        factory.activateObject(pooledObject);
                        pooledObject.recordBorrow();
                        active.incrementAndGet();
                        object = pooledObject.getObject();
                        allocatedObjects.computeIfAbsent(key, k -> new HashMap<>()).put(object, pooledObject);
                        return object;
                    }
                }

                if (defaultConfig.isBlockWhenExhausted()) {
                    long maxWait = defaultConfig.getMaxWaitMillis();
                    if (maxWait <= 0) {
                        condition.await();
                    } else {
                        condition.await(maxWait, TimeUnit.MILLISECONDS);
                    }
                } else {
                    throw new NoSuchElementException("Pool exhausted for key: " + key);
                }
            }
        } finally {
            lock.unlock();
        }

        return object;
    }

    @Override
    /**
     * returnObject方法。
     *      * @param key K类型参数
     * @param obj V类型参数
     */
    public void returnObject(K key, V obj) throws Exception {
        checkOpen();

        PooledObjectStack<V> stack = pools.get(key);
        AtomicInteger active = activeCount.get(key);

        if (stack == null || active == null) {
            return;
        }

        lock.lock();
        try {
            Map<V, PooledObject<V>> keyAllocated = allocatedObjects.get(key);
            PooledObject<V> pooledObject = keyAllocated != null ? keyAllocated.remove(obj) : null;

            if (pooledObject == null) {
                // 如果找不到，尝试在栈中查找
                pooledObject = stack.find(obj);
                if (pooledObject == null) {
                    return;
                }
            }

            pooledObject.returnObject();

            if (defaultConfig.isTestOnReturn() && !factory.validateObject(pooledObject)) {
                destroyObject(key, pooledObject);
                condition.signalAll();
                return;
            }

            factory.passivateObject(pooledObject);

            if (closed || !pooledObject.isIdle()) {
                destroyObject(key, pooledObject);
                condition.signalAll();
                return;
            }

            if (stack.size() < defaultConfig.getMaxIdle()) {
                stack.push(pooledObject);
            } else {
                destroyObject(key, pooledObject);
            }

            condition.signalAll();

        } finally {
            active.decrementAndGet();
            lock.unlock();
        }
    }

    @Override
    /**
     * invalidateObject方法。
     *      * @param key K类型参数
     * @param obj V类型参数
     */
    public void invalidateObject(K key, V obj) throws Exception {
        lock.lock();
        try {
            PooledObjectStack<V> stack = pools.get(key);
            AtomicInteger active = activeCount.get(key);

            if (stack == null || active == null) {
                return;
            }

            // 先从 allocatedObjects 中移除
            Map<V, PooledObject<V>> keyAllocated = allocatedObjects.get(key);
            PooledObject<V> pooledObject = keyAllocated != null ? keyAllocated.remove(obj) : null;

            // 如果没找到，尝试在栈中查找
            if (pooledObject == null) {
                pooledObject = stack.remove(obj);
            }

            if (pooledObject != null) {
                pooledObject.invalidate();
                destroyObject(key, pooledObject);
                active.decrementAndGet();
            }

            condition.signalAll();

        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * clear方法。
     */
    public void clear() throws Exception {
        lock.lock();
        try {
            for (K key : new ArrayList<>(pools.keySet())) {
                clear(key);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * clear方法。
     *      * @param key K类型参数
     */
    public void clear(K key) throws Exception {
        lock.lock();
        try {
            PooledObjectStack<V> stack = pools.remove(key);
            if (stack != null) {
                Iterator<PooledObject<V>> it = stack.iterator();
                while (it.hasNext()) {
                    destroyObject(key, it.next());
                }
            }
            activeCount.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * getPoolSize方法。
     * @return int类型返回值
     */
    public int getPoolSize() {
        int size = 0;
        for (PooledObjectStack<V> stack : pools.values()) {
            size += stack.size();
        }
        return size;
    }

    @Override
    /**
     * getNumIdle方法。
     * @return int类型返回值
     */
    public int getNumIdle() {
        return getPoolSize();
    }

    @Override
    /**
     * getNumActive方法。
     * @return int类型返回值
     */
    public int getNumActive() {
        int total = 0;
        for (AtomicInteger count : activeCount.values()) {
            total += count.get();
        }
        return total;
    }

    @Override
    /**
     * getNumIdle方法。
     *      * @param key K类型参数
     * @return int类型返回值
     */
    public int getNumIdle(K key) {
        PooledObjectStack<V> stack = pools.get(key);
        return stack != null ? stack.size() : 0;
    }

    @Override
    /**
     * getNumActive方法。
     *      * @param key K类型参数
     * @return int类型返回值
     */
    public int getNumActive(K key) {
        AtomicInteger count = activeCount.get(key);
        return count != null ? count.get() : 0;
    }

    @Override
    /**
     * close方法。
     */
    public void close() {
        if (closed) {
            return;
        }

        lock.lock();
        try {
            closed = true;
            condition.signalAll();

            for (K key : new ArrayList<>(pools.keySet())) {
                try {
                    clear(key);
                } catch (Exception e) {
                    // 忽略清理错误
                }
            }

            pools.clear();
            allocatedObjects.clear();
            activeCount.clear();

        } finally {
            lock.unlock();
        }
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("Pool is closed");
        }
    }

    private void destroyObject(K key, PooledObject<V> pooledObject) {
        try {
            factory.destroyObject(pooledObject);
        } catch (Exception e) {
            // 忽略
        }
    }

    /**
     * 内部栈实现
     */
    private class PooledObjectStack<V> {
        private final Deque<PooledObject<V>> deque = new ArrayDeque<>();

        void push(PooledObject<V> p) {
            deque.push(p);
        }

        PooledObject<V> pop() {
            return deque.pollFirst();
        }

        PooledObject<V> find(V obj) {
            for (PooledObject<V> p : deque) {
                if (p.getObject() == obj) {
                    return p;
                }
            }
            return null;
        }

        PooledObject<V> remove(V obj) {
            Iterator<PooledObject<V>> it = deque.iterator();
            while (it.hasNext()) {
                PooledObject<V> p = it.next();
                if (p.getObject() == obj) {
                    it.remove();
                    return p;
                }
            }
            return null;
        }

        int size() {
            return deque.size();
        }

        Iterator<PooledObject<V>> iterator() {
            return deque.iterator();
        }
    }
}
