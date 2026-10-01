package com.zifang.util.core.pattern.pool;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于软引用的对象池
 * <p>
 * 当内存不足时，对象会被自动回收
 *
 * @param <T> 对象类型
 */
public class SoftReferenceObjectPool<T> implements ObjectPool<T> {

    private final PooledObjectFactory<T> factory;
    private final PoolConfig config;

    private final Queue<SoftReference<PooledObject<T>>> idleObjects;
    private final AtomicInteger numActive;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile boolean closed;

    /**
     * SoftReferenceObjectPool方法。
     * * @param factory PooledObjectFactoryT类型参数
     */
    public SoftReferenceObjectPool(PooledObjectFactory<T> factory) {
        this(factory, new PoolConfig());
    }

    /**
     * SoftReferenceObjectPool方法。
     * * @param factory PooledObjectFactoryT类型参数
     *
     * @param config PoolConfig类型参数
     */
    public SoftReferenceObjectPool(PooledObjectFactory<T> factory, PoolConfig config) {
        this.factory = factory;
        this.config = config;
        this.idleObjects = new LinkedList<>();
        this.numActive = new AtomicInteger(0);
        this.closed = false;
    }

    @Override
    /**
     * borrowObject方法。
     * @return T类型返回值
     */
    public T borrowObject() throws Exception {
        checkOpen();

        while (true) {
            T object = null;
            PooledObject<T> pooledObject = null;

            lock.lock();
            try {
                // 清理已被回收的软引用
                cleanUpReferences();

                // 尝试获取空闲对象
                Iterator<SoftReference<PooledObject<T>>> it = idleObjects.iterator();
                while (it.hasNext()) {
                    SoftReference<PooledObject<T>> ref = it.next();
                    pooledObject = ref.get();

                    if (pooledObject == null) {
                        it.remove();
                        continue;
                    }

                    if (config.isTestOnBorrow() && !factory.validateObject(pooledObject)) {
                        destroyObject(pooledObject);
                        it.remove();
                        continue;
                    }

                    if (pooledObject.allocate()) {
                        factory.activateObject(pooledObject);
                        object = pooledObject.getObject();
                        pooledObject.recordBorrow();
                        it.remove();
                        return object;
                    }
                }

                // 尝试创建新对象
                if (numActive.get() < config.getMaxTotal()) {
                    pooledObject = factory.makeObject();
                    if (pooledObject != null) {
                        pooledObject.allocate();
                        factory.activateObject(pooledObject);
                        pooledObject.recordBorrow();
                        numActive.incrementAndGet();
                        return pooledObject.getObject();
                    }
                }

                // 阻塞等待
                if (config.isBlockWhenExhausted()) {
                    long maxWait = config.getMaxWaitMillis();
                    if (maxWait <= 0) {
                        condition.await();
                    } else {
                        long nanos = TimeUnit.MILLISECONDS.toNanos(maxWait);
                        while (nanos > 0) {
                            nanos = condition.awaitNanos(nanos);
                            // 检查是否有可用的对象
                            if (!idleObjects.isEmpty()) {
                                break;
                            }
                        }
                    }
                } else {
                    throw new NoSuchElementException("Pool exhausted");
                }

            } finally {
                lock.unlock();
            }
        }
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
            cleanUpReferences();

            PooledObject<T> pooledObject = findPooledObject(obj);
            if (pooledObject == null) {
                pooledObject = new PooledObject<>(obj);
            }

            pooledObject.returnObject();

            if (config.isTestOnReturn() && !factory.validateObject(pooledObject)) {
                destroyObject(pooledObject);
                return;
            }

            factory.passivateObject(pooledObject);

            if (closed || !pooledObject.isIdle()) {
                destroyObject(pooledObject);
                return;
            }

            if (idleObjects.size() < config.getMaxIdle()) {
                idleObjects.add(new SoftReference<>(pooledObject));
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
            PooledObject<T> pooledObject = findPooledObject(obj);
            if (pooledObject != null) {
                pooledObject.invalidate();
                destroyObject(pooledObject);
                idleObjects.removeIf(ref -> ref.get() == pooledObject);
                numActive.decrementAndGet();
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
            idleObjects.clear();
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
            cleanUpReferences();
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

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("Pool is closed");
        }
    }

    private PooledObject<T> findPooledObject(T obj) {
        for (SoftReference<PooledObject<T>> ref : idleObjects) {
            PooledObject<T> p = ref.get();
            if (p != null && p.getObject() == obj) {
                return p;
            }
        }
        return null;
    }

    private void cleanUpReferences() {
        idleObjects.removeIf(ref -> ref.get() == null);
    }

    private void destroyObject(PooledObject<T> pooledObject) {
        try {
            factory.destroyObject(pooledObject);
            numActive.decrementAndGet();
        } catch (Exception e) {
            // 忽略
        }
    }
}
