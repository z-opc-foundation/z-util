package com.zifang.util.core.pattern.pool;

/**
 * 池化对象工厂接口
 *
 * @param <T> 对象类型
 */
public interface PooledObjectFactory<T> {

    /**
     * 创建新对象
     */
    PooledObject<T> makeObject();

    /**
     * 销毁对象
     */
    void destroyObject(PooledObject<T> p);

    /**
     * 验证对象是否有效
     */
    boolean validateObject(PooledObject<T> p);

    /**
     * 激活对象（借用前调用）
     */
    void activateObject(PooledObject<T> p);

    /**
     * 钝化对象（归还后调用）
     */
    void passivateObject(PooledObject<T> p);
}
