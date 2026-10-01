package com.zifang.util.core.pattern.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册器。
 * <p>
 * 提供键值对形式的注册功能。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author zifang
 */
public class Register<K, V> {

    private final Map<K, V> registry = new ConcurrentHashMap<>();

    /**
     * 注册键值对。
     *
     * @param key   键
     * @param value 值
     */
    public void register(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        registry.put(key, value);
    }

    /**
     * 根据键获取值。
     *
     * @param key 键
     * @return 值，如果不存在返回null
     */
    public V get(K key) {
        return registry.get(key);
    }

    /**
     * 取消注册。
     *
     * @param key 键
     * @return 被移除的值，如果不存在返回null
     */
    public V unregister(K key) {
        return registry.remove(key);
    }

    /**
     * 获取所有注册的键值对。
     *
     * @return 不可修改的键值对列表
     */
    public List<Map.Entry<K, V>> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(registry.entrySet()));
    }

    /**
     * 判断是否包含指定键。
     *
     * @param key 键
     * @return 是否包含
     */
    public boolean contains(K key) {
        return registry.containsKey(key);
    }

    /**
     * 清除所有注册项。
     */
    public void clear() {
        registry.clear();
    }

    /**
     * 获取注册数量。
     *
     * @return 数量
     */
    public int size() {
        return registry.size();
    }
}