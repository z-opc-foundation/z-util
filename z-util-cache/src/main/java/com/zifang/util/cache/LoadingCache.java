package com.zifang.util.cache;

import java.util.Map;
import java.util.function.Function;

/**
 * 加载型缓存：所有读操作在 key 缺失时自动调用 {@link CacheLoader} 加载。
 * <p>
 * 与 {@link Cache#get(Object, Function)} 的区别：loader 是绑定在缓存上的（一次性注册），
 * 调用更简洁；并且支持批量加载、刷新等。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface LoadingCache<K, V> extends Cache<K, V> {

    /**
     * 取值；缺失时通过 {@link CacheLoader} 加载。可选支持并发击穿防护（按实现）。
     */
    @Override
    V get(K key);

    /**
     * 批量取。每个缺失的 key 都会通过 loader 加载。
     */
    Map<K, V> getAll(Iterable<? extends K> keys);

    /**
     * 主动刷新一个 key 的值。返回新值（无论是否变更）。失败的语义由实现决定。
     */
    V refresh(K key);
}
