package com.zifang.util.core.pattern.cache;

/**
 * 缓存客户端接口，提供基本的缓存操作能力
 *
 * @author zifang
 */
public interface CacheClient {

    /**
     * 根据键获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，如果键不存在则返回null
     */
    Object get(String key);

    /**
     * 设置缓存键值对
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);
}