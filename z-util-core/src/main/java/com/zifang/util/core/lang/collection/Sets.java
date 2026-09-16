package com.zifang.util.core.lang.collection;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author zifang
 */
public class Sets {

    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param ts  元素数组
     * @return HashSet对象
     */
    @SafeVarargs
    /**
     * newHashSet方法。
     *      * @param ts T...类型参数
     * @return static <T> HashSet<T>类型返回值
     */
    public static <T> HashSet<T> newHashSet(T... ts) {
        return newHashSet(false, ts);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>      集合元素类型
     * @param isSorted 是否有序，有序返回 {@link LinkedHashSet}，否则返回 {@link HashSet}
     * @param ts       元素数组
     * @return HashSet对象
     */
    @SafeVarargs
    /**
     * newHashSet方法。
     *      * @param isSorted boolean类型参数
     * @param ts T...类型参数
     * @return static <T> HashSet<T>类型返回值
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, T... ts) {
        if (null == ts) {
            return isSorted ? new LinkedHashSet<>() : new HashSet<>();
        }
        int initialCapacity = Math.max((int) (ts.length / .75f) + 1, 16);
        final HashSet<T> set = isSorted ? new LinkedHashSet<>(initialCapacity) : new HashSet<>(initialCapacity);
        Collections.addAll(set, ts);
        return set;
    }

    /**
     * 计算两个集合的交集
     *
     * @param <T>  集合元素类型
     * @param set1 第一个集合
     * @param set2 第二个集合
     * @return 交集集合
     */
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        if (set1 == null || set2 == null) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }

    /**
     * 计算两个集合的差集
     *
     * @param <T>  集合元素类型
     * @param set1 第一个集合
     * @param set2 第二个集合
     * @return 差集集合
     */
    public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        if (set1 == null) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(set1);
        if (set2 != null) {
            result.removeAll(set2);
        }
        return result;
    }

    /**
     * 计算两个集合的并集
     *
     * @param <T>  集合元素类型
     * @param set1 第一个集合
     * @param set2 第二个集合
     * @return 并集集合
     */
    public static <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> result = new HashSet<>();
        if (set1 != null) {
            result.addAll(set1);
        }
        if (set2 != null) {
            result.addAll(set2);
        }
        return result;
    }
}
