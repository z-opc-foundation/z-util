package com.zifang.util.core.pattern.cache;

/**
 * 缓存客户端默认实现
 *
 * @author zifang
 */
public class DefaultCacheClient implements CacheClient {

    private CacheProvider cacheProvider;

    /**
     * 设置缓存键值对
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    @Override
    /**
     * set方法。
     *      * @param key String类型参数
     * @param value Object类型参数
     */
    public void set(String key, Object value) {

    }

    /**
     * 根据键获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，如果键不存在则返回null
     */
    @Override
    /**
     * get方法。
     *      * @param key String类型参数
     * @return Object类型返回值
     */
    public Object get(String key) {

        return "";
    }
}