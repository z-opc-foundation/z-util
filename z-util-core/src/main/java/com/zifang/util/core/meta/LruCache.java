package com.zifang.util.core.meta;

import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: zifang
 * @time: 2022-02-09 16:26:01
 * @description: lru cache
 * @version: JDK 1.8
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = -5167631809472116969L;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final int DEFAULT_MAX_CAPACITY = 1000;
    private final Lock lock = new ReentrantLock();
    private volatile int maxCapacity;

    /**
     * LruCache方法。
     */
    public LruCache() {
        this(DEFAULT_MAX_CAPACITY);
    }

    /**
     * LruCache方法。
     * * @param maxCapacity int类型参数
     */
    public LruCache(int maxCapacity) {
        super(16, DEFAULT_LOAD_FACTOR, true);
        this.maxCapacity = maxCapacity;
    }

    @Override
    /**
     * removeEldestEntry方法。
     *      * @param eldest java.util.Map.EntryK,类型参数
     * @return boolean类型返回值
     */
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > maxCapacity;
    }

    @Override
    /**
     * containsKey方法。
     *      * @param key Object类型参数
     * @return boolean类型返回值
     */
    public boolean containsKey(Object key) {
        lock.lock();
        try {
            return super.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * get方法。
     *      * @param key Object类型参数
     * @return V类型返回值
     */
    public V get(Object key) {
        lock.lock();
        try {
            return super.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * put方法。
     *      * @param key K类型参数
     * @param value V类型参数
     * @return V类型返回值
     */
    public V put(K key, V value) {
        lock.lock();
        try {
            return super.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * remove方法。
     *      * @param key Object类型参数
     * @return V类型返回值
     */
    public V remove(Object key) {
        lock.lock();
        try {
            return super.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * size方法。
     * @return int类型返回值
     */
    public int size() {
        lock.lock();
        try {
            return super.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    /**
     * clear方法。
     */
    public void clear() {
        lock.lock();
        try {
            super.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * getMaxCapacity方法。
     *
     * @return int类型返回值
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * setMaxCapacity方法。
     * * @param maxCapacity int类型参数
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

}
