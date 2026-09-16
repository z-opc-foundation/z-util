package com.zifang.util.core.concurrency.packages;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 使用读写锁实现缓存的例子
 */

/**
 * CacheDemo类。
 */
public class CacheDemo {

    private Map<String, Object> cache = new HashMap<String, Object>();
    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

    }

    /**
     * getData方法。
     * * @param key String类型参数
     *
     * @return synchronized Object类型返回值
     */
    public synchronized Object getData(String key) {
        rwl.readLock().lock();
        Object value = null;
        try {
            value = cache.get(key);
            if (value == null) {
                rwl.readLock().unlock();
                rwl.writeLock().lock();
                try {
                    if (value == null) {
                        value = "aaaa"; //实际是去 queryDB();
                    }
                } finally {
                    rwl.writeLock().unlock();
                }
                rwl.readLock().lock();
            }
        } finally {
            rwl.readLock().unlock();
        }
        return value;
    }
}
