package com.zifang.util.core.pattern.pool;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于栈的对象池实现
 * <p>
 * 使用Deque存储空闲对象，LIFO顺序
 *
 * @param <T> 对象类型
 */
public class StackObjectPool<T> implements ObjectPool<T> {

    private final PooledObjectFactory<T> factory;
    private final PoolConfig config;

    private final Deque<PooledObject<T>> idleObjects;
    private final Map<T, PooledObject<T>> allocatedObjects;
    private final AtomicInteger numActive;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile boolean closed;
    private EvictionTimer evictionTimer;

    /**
     * StackObjectPool方法。
     * * @param factory PooledObjectFactoryT类型参数
     */
    public StackObjectPool(PooledObjectFactory<T> factory) {
        this(factory, new PoolConfig());
    }

    /**
     * StackObjectPool方法。
     * * @param factory PooledObjectFactoryT类型参数
     *
     * @param config PoolConfig类型参数
     */
    public StackObjectPool(PooledObjectFactory<T> factory, PoolConfig config) {
        this(factory, config, config.getMaxTotal());
    }

    /**
     * StackObjectPool方法。
     * * @param factory PooledObjectFactoryT类型参数
     *
     * @param config      PoolConfig类型参数
     * @param maxPoolSize int类型参数
     */
    public StackObjectPool(PooledObjectFactory<T> factory, PoolConfig config, int maxPoolSize) {
        this.factory = factory;
        this.config = config;
        this.idleObjects = new ArrayDeque<>(maxPoolSize);
        this.allocatedObjects = new HashMap<>();
        this.numActive = new AtomicInteger(0);
        this.closed = false;

        // 初始化最小空闲对象
        int minIdle = config.getMinIdle();
        for (int i = 0; i < minIdle; i++) {
            try {
                addObject();
            } catch (Exception e) {
                // 忽略初始化错误
                break;
            }
        }

        // 启动 eviction 定时器
        if (config.getDurationBetweenEvictionRuns() > 0) {
            evictionTimer = new EvictionTimer(this, config.getDurationBetweenEvictionRuns());
        }
    }

    @Override
    /**
     * borrowObject方法。
     * @return T类型返回值
     */
    public T borrowObject() throws Exception {
        checkOpen();

        T object = null;
        PooledObject<T> pooledObject = null;

        lock.lock();
        try {
            while (object == null) {
                if (closed) {
                    throw new IllegalStateException("Pool is closed");
                }

                // 尝试从空闲栈获取
                while (!idleObjects.isEmpty()) {
                    pooledObject = idleObjects.pollFirst();
                    if (pooledObject == null) {
                        continue;
                    }

                    // 验证对象
                    if (config.isTestOnBorrow() && !factory.validateObject(pooledObject)) {
                        destroyObject(pooledObject);
                        pooledObject = null;
                        continue;
                    }

                    // 分配对象
                    if (pooledObject.allocate()) {
                        factory.activateObject(pooledObject);
                        object = pooledObject.getObject();
                        pooledObject.recordBorrow();
                        allocatedObjects.put(object, pooledObject);
                        numActive.incrementAndGet();
                        return object;
                    }
                }

                // 尝试创建新对象
                if (numActive.get() < config.getMaxTotal()) {
                    pooledObject = factory.makeObject();
                    if (pooledObject != null) {
                        pooledObject.allocate();
                        // 验证新创建的对象
                        if (config.isTestOnBorrow() && !factory.validateObject(pooledObject)) {
                            destroyObject(pooledObject);
                            pooledObject = null;
                            continue;
                        }
                        factory.activateObject(pooledObject);
                        pooledObject.recordBorrow();
                        numActive.incrementAndGet();
                        object = pooledObject.getObject();
                        allocatedObjects.put(object, pooledObject);
                        return object;
                    }
                }

                // 阻塞等待
                if (config.isBlockWhenExhausted()) {
                    long maxWait = config.getMaxWaitMillis();
                    if (maxWait <= 0) {
                        condition.await();
                    } else {
                        long nanos = TimeUnit.MILLISECONDS.toNanos(maxWait);
                        while (nanos > 0 && object == null) {
                            nanos = condition.awaitNanos(nanos);
                        }
                        if (object == null) {
                            throw new NoSuchElementException("Timeout waiting for object");
                        }
                    }
                } else {
                    throw new NoSuchElementException("Pool exhausted");
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
     *      * @param obj T类型参数
     */
    public void returnObject(T obj) throws Exception {
        checkOpen();

        lock.lock();
        try {
            // 找到对应的 pooledObject
            PooledObject<T> pooledObject = allocatedObjects.remove(obj);
            if (pooledObject == null) {
                return;
            }

            // 归还
            pooledObject.returnObject();

            // 验证
            if (config.isTestOnReturn() && !factory.validateObject(pooledObject)) {
                destroyObject(pooledObject);
                numActive.decrementAndGet();
                condition.signalAll();
                return;
            }

            // 钝化
            factory.passivateObject(pooledObject);

            // 检查是否应该放入空闲队列
            if (closed || !pooledObject.isIdle()) {
                destroyObject(pooledObject);
                numActive.decrementAndGet();
                condition.signalAll();
                return;
            }

            // 放入空闲栈
            if (idleObjects.size() < config.getMaxIdle()) {
                idleObjects.addFirst(pooledObject);
            } else {
                destroyObject(pooledObject);
            }

            numActive.decrementAndGet();
            condition.signalAll();

        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * invalidateObject方法。
     *      * @param obj T类型参数
     */
    public void invalidateObject(T obj) throws Exception {
        lock.lock();
        try {
            PooledObject<T> pooledObject = allocatedObjects.remove(obj);
            if (pooledObject == null) {
                return;
            }

            pooledObject.invalidate();
            destroyObject(pooledObject);
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
            List<PooledObject<T>> toDestroy = new ArrayList<>(idleObjects);
            idleObjects.clear();

            for (PooledObject<T> p : toDestroy) {
                destroyObject(p);
            }
        } finally {
            lock.unlock();
        }
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

            if (evictionTimer != null) {
                evictionTimer.cancel();
            }

            try {
                clear();
            } catch (Exception e) {
                // 忽略清理错误
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * getNumActive方法。
     * @return int类型返回值
     */
    public int getNumActive() {
        return numActive.get();
    }

    @Override
    /**
     * getNumIdle方法。
     * @return int类型返回值
     */
    public int getNumIdle() {
        lock.lock();
        try {
            return idleObjects.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * getConfig方法。
     * @return PoolConfig类型返回值
     */
    public PoolConfig getConfig() {
        return config;
    }

    // 内部方法

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("Pool is closed");
        }
    }

    private PooledObject<T> findPooledObject(T obj) {
        // 先从 allocatedObjects 中查找
        PooledObject<T> p = allocatedObjects.get(obj);
        if (p != null) {
            return p;
        }
        // 再从 idleObjects 中查找
        for (PooledObject<T> pooledObject : idleObjects) {
            if (pooledObject.getObject() == obj) {
                return pooledObject;
            }
        }
        return null;
    }

    private void destroyObject(PooledObject<T> pooledObject) {
        try {
            factory.destroyObject(pooledObject);
            numActive.decrementAndGet();
        } catch (Exception e) {
            // 忽略销毁错误
        }
    }

    /**
     * 添加新对象到池
     */
    public void addObject() throws Exception {
        lock.lock();
        try {
            if (idleObjects.size() >= config.getMaxIdle()) {
                return;
            }

            PooledObject<T> pooledObject = factory.makeObject();
            if (pooledObject != null) {
                pooledObject.returnObject();

                if (idleObjects.size() < config.getMaxIdle()) {
                    idleObjects.addFirst(pooledObject);
                    factory.passivateObject(pooledObject);
                } else {
                    destroyObject(pooledObject);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 执行 eviction
     */
    void evict() {
        lock.lock();
        try {
            if (idleObjects.isEmpty()) {
                return;
            }

            long now = System.currentTimeMillis();
            List<PooledObject<T>> toEvict = new ArrayList<>();

            for (PooledObject<T> p : idleObjects) {
                if (p.startEvictionTest()) {
                    toEvict.add(p);
                }
            }

            for (PooledObject<T> p : toEvict) {
                boolean evictable = true;
                if (config.isTestWhileIdle()) {
                    evictable = factory.validateObject(p);
                }

                p.endEvictionTest(evictable);

                if (!evictable) {
                    idleObjects.remove(p);
                    destroyObject(p);
                }
            }

            // 确保最小空闲对象数量
            while (idleObjects.size() < config.getMinIdle()) {
                try {
                    addObject();
                } catch (Exception e) {
                    break;
                }
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * Eviction 定时器
     */
    static class EvictionTimer {
        private final ScheduledFuture<?> future;
        private final StackObjectPool<?> pool;

        EvictionTimer(final StackObjectPool<?> pool, long delay) {
            this.pool = pool;
            this.future = Executors.newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(() -> pool.evict(), delay, delay, TimeUnit.MILLISECONDS);
        }

        void cancel() {
            future.cancel(false);
        }
    }
}
