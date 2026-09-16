package com.zifang.util.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link LoadingCache} 的内存实现。
 * <p>
 * 用 {@link ConcurrentHashMap} 维护 pending 集合实现"按 key 合并加载"，
 * 防止同一 key 在并发 miss 时被重复加载。
 */
public class LoadingMemoryCache<K, V> extends MemoryCache<K, V> implements LoadingCache<K, V> {

    private final CacheLoader<K, V> loader;
    private final ConcurrentHashMap<K, Object> loadingLocks = new ConcurrentHashMap<>();

    LoadingMemoryCache(CacheBuilder<K, V> b, CacheLoader<K, V> loader) {
        super(b);
        this.loader = loader;
    }

    @Override
    public V get(K key) {
        V cached = get(key, k -> {
            try {
                return loader.load(k);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException("CacheLoader failed for key " + k, ex);
            }
        });
        return cached;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        for (K k : keys) {
            V v = get(k);
            if (v != null) result.put(k, v);
        }
        return result;
    }

    @Override
    public V refresh(K key) {
        try {
            V loaded = loader.load(key);
            if (loaded != null) {
                put(key, loaded);
            }
            return loaded;
        } catch (Exception e) {
            throw new RuntimeException("CacheLoader failed for key " + key, e);
        }
    }
}
