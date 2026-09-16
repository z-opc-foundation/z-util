package com.zifang.util.core.lang.collection;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zifang
 */
public class Maps {

    /**
     * 移除Map中key为null的元素
     *
     * @param map 待处理Map，不能为null
     * @throws NullPointerException 如果map为null
     */
    public static <K, V> void removeNullKeys(Map<K, V> map) {
        removeKeys(map, Objects::isNull);
    }

    /**
     * 移除Map中value为null的元素
     *
     * @param map 待处理Map，不能为null
     * @throws NullPointerException 如果map为null
     */
    public static <K, V> void removeNullValues(Map<K, V> map) {
        removeValues(map, Objects::isNull);
    }


    /**
     * 移除Map中符合predicate检测结果的key的元素
     *
     * @param map       待处理Map，不能为null
     * @param predicate 检验条件，用于判断key是否需要被移除
     * @throws NullPointerException 如果map或predicate为null
     */
    public static <K, V> void removeKeys(Map<K, V> map, Predicate<K> predicate) {
        map.entrySet().removeIf(entry -> predicate.test(entry.getKey()));
    }


    /**
     * 移除Map中符合predicate检测结果的value的元素
     *
     * @param map       待处理Map，不能为null
     * @param predicate 检验条件，用于判断value是否需要被移除
     * @throws NullPointerException 如果map或predicate为null
     */
    public static <K, V> void removeValues(Map<K, V> map, Predicate<V> predicate) {
        map.entrySet().removeIf(entry -> predicate.test(entry.getValue()));
    }

    /**
     * 移除Map中符合predicate检测结果的key和value的元素
     *
     * @param map       待处理Map，不能为null
     * @param predicate 检验条件，同时对key和value进行判断
     * @throws NullPointerException 如果map或predicate为null
     */
    public static <K, V> void remove(Map<K, V> map, Predicate<Map.Entry<K, V>> predicate) {
        map.entrySet().removeIf(predicate);
    }

    /**
     * 对给定的Map按照条件进行过滤，返回符合条件的元素组成的新Map
     *
     * @param map       待过滤的Map，不能为null
     * @param predicate 过滤条件，用于判断元素是否应被保留
     * @return Map<K, V> 符合条件的新Map，如果没有任何元素符合条件则返回空Map
     * @throws NullPointerException 如果map或predicate为null
     */
    public static <K, V> Map<K, V> filter(Map<K, V> map, Predicate<Map.Entry<K, V>> predicate) {
        Map<K, V> mapStore = new HashMap<>(map.size());
        remove(mapStore, predicate);
        return mapStore;
    }

    /**
     * 根据Set集合中的每个元素，分裂生成对应的Map对象
     *
     * @param set           待处理的Set集合，不能为null
     * @param acceptAsKey   将Set中每个元素转换为Map的key的函数，不能为null
     * @param acceptAsValue 将Set中每个元素转换为Map的value的函数，不能为null
     * @return Map<K, V> 根据给定规则生成的Map，key由acceptAsKey函数产生，value由acceptAsValue函数产生
     * @throws NullPointerException 如果set、acceptAsKey或acceptAsValue为null
     */
    public static <U, K, V> Map<K, V> populateMap(Set<U> set, Function<U, K> acceptAsKey, Function<U, V> acceptAsValue) {
        Map<K, V> map = new LinkedHashMap<>();
        for (U u : set) {
            K k = acceptAsKey.apply(u);
            V v = acceptAsValue.apply(u);
            map.put(k, v);
        }
        return map;
    }

    /**
     * Java 8 兼容的 Map.of() 替代方案
     */
    public static <K, V> java.util.Map<K, V> of(K k1, V v1) {
        java.util.Map<K, V> m = new java.util.LinkedHashMap<>(2);
        m.put(k1, v1);
        return java.util.Collections.unmodifiableMap(m);
    }

    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2) {
        java.util.Map<K, V> m = new java.util.LinkedHashMap<>(4);
        m.put(k1, v1);
        m.put(k2, v2);
        return java.util.Collections.unmodifiableMap(m);
    }

    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        java.util.Map<K, V> m = new java.util.LinkedHashMap<>(8);
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        return java.util.Collections.unmodifiableMap(m);
    }

    public static <K, V> java.util.Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        java.util.Map<K, V> m = new java.util.LinkedHashMap<>(8);
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        m.put(k4, v4);
        return java.util.Collections.unmodifiableMap(m);
    }
}
