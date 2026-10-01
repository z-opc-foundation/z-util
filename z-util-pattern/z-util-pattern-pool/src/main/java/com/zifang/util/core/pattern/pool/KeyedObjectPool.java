package com.zifang.util.core.pattern.pool;

/**
 * 带键的对象池接口
 *
 * @param <K> 键类型
 * @param <V> 对象类型
 */
public interface KeyedObjectPool<K, V> {

    /**
     * 借用对象
     */
    V borrowObject(K key) throws Exception;

    /**
     * 归还对象
     */
    void returnObject(K key, V obj) throws Exception;

    /**
     * 使对象无效
     */
    void invalidateObject(K key, V obj) throws Exception;

    /**
     * 清空池
     */
    void clear() throws Exception;

    /**
     * 清空指定键的池
     */
    void clear(K key) throws Exception;

    /**
     * 获取池大小
     */
    int getPoolSize();

    /**
     * 获取空闲对象数
     */
    int getNumIdle();

    /**
     * 获取活跃对象数
     */
    int getNumActive();

    /**
     * 获取指定键的空闲对象数
     */
    int getNumIdle(K key);

    /**
     * 获取指定键的活跃对象数
     */
    int getNumActive(K key);

    /**
     * 关闭池
     */
    void close();
}
