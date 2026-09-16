package com.zifang.util.core.pattern.pool;

import com.zifang.util.core.pattern.pool.monitor.PoolListener;
import com.zifang.util.core.pattern.pool.monitor.PoolMonitor;
import com.zifang.util.core.pattern.pool.monitor.PoolStats;

import java.util.NoSuchElementException;

/**
 * 带监控的对象池包装器
 * <p>
 * 包装现有的ObjectPool实现，添加监控能力
 *
 * @param <T> 对象类型
 */
public class MonitoredObjectPool<T> implements ObjectPool<T> {

    private final ObjectPool<T> pool;
    private final PoolMonitor<T> monitor;

    /**
     * MonitoredObjectPool方法。
     * * @param pool ObjectPoolT类型参数
     *
     * @param config   PoolConfig类型参数
     * @param listener PoolListenerT类型参数
     */
    public MonitoredObjectPool(ObjectPool<T> pool, PoolConfig config, PoolListener<T> listener) {
        this.pool = pool;
        this.monitor = new PoolMonitor<>(listener);
    }

    /**
     * MonitoredObjectPool方法。
     * * @param factory PooledObjectFactoryT类型参数
     *
     * @param config   PoolConfig类型参数
     * @param listener PoolListenerT类型参数
     */
    public MonitoredObjectPool(PooledObjectFactory<T> factory, PoolConfig config, PoolListener<T> listener) {
        this.pool = new StackObjectPool<>(factory, config);
        this.monitor = new PoolMonitor<>(listener);
    }

    /**
     * getMonitor方法。
     *
     * @return PoolMonitor<T>类型返回值
     */
    public PoolMonitor<T> getMonitor() {
        return monitor;
    }

    /**
     * getStats方法。
     *
     * @return PoolStats类型返回值
     */
    public PoolStats getStats() {
        return monitor.getStats();
    }

    @Override
    /**
     * borrowObject方法。
     * @return T类型返回值
     */
    public T borrowObject() throws Exception {
        long start = System.currentTimeMillis();
        try {
            T obj = pool.borrowObject();
            monitor.recordBorrow(obj, System.currentTimeMillis() - start);
            return obj;
        } catch (NoSuchElementException e) {
            monitor.recordBorrow(null, System.currentTimeMillis() - start);
            throw e;
        }
    }

    @Override
    /**
     * returnObject方法。
     *      * @param obj T类型参数
     */
    public void returnObject(T obj) throws Exception {
        long start = System.currentTimeMillis();
        try {
            pool.returnObject(obj);
            monitor.recordReturn(obj, System.currentTimeMillis() - start);
        } catch (Exception e) {
            monitor.recordReturn(obj, System.currentTimeMillis() - start);
            throw e;
        }
    }

    @Override
    /**
     * invalidateObject方法。
     *      * @param obj T类型参数
     */
    public void invalidateObject(T obj) throws Exception {
        pool.invalidateObject(obj);
    }

    @Override
    /**
     * clear方法。
     */
    public void clear() throws Exception {
        pool.clear();
    }

    @Override
    /**
     * close方法。
     */
    public void close() {
        try {
            pool.close();
        } finally {
            monitor.recordClose();
        }
    }

    @Override
    /**
     * getNumActive方法。
     * @return int类型返回值
     */
    public int getNumActive() {
        return pool.getNumActive();
    }

    @Override
    /**
     * getNumIdle方法。
     * @return int类型返回值
     */
    public int getNumIdle() {
        return pool.getNumIdle();
    }

    @Override
    /**
     * getConfig方法。
     * @return PoolConfig类型返回值
     */
    public PoolConfig getConfig() {
        return pool.getConfig();
    }
}