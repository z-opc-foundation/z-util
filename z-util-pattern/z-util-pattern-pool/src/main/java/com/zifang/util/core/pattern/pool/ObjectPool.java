package com.zifang.util.core.pattern.pool;

import java.util.NoSuchElementException;

/**
 * 对象池接口
 *
 * @param <T> 对象类型
 */
public interface ObjectPool<T> {

    /**
     * 借用对象
     *
     * @return 池化对象
     * @throws NoSuchElementException 如果池 exhausted 且 blocking 模式下超时
     * @throws IllegalStateException  如果池 closed
     */
    T borrowObject() throws Exception;

    /**
     * 归还对象
     *
     * @param obj 要归还的对象
     */
    void returnObject(T obj) throws Exception;

    /**
     * 使对象无效
     *
     * @param obj 要废弃的对象
     */
    void invalidateObject(T obj) throws Exception;

    /**
     * 清空池
     */
    void clear() throws Exception;

    /**
     * 关闭池
     */
    void close();

    /**
     * 获取活跃对象数量
     */
    int getNumActive();

    /**
     * 获取空闲对象数量
     */
    int getNumIdle();

    /**
     * 获取配置
     */
    PoolConfig getConfig();
}
