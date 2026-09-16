package com.zifang.util.core.lang.collection;


import com.zifang.util.core.lang.validator.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/***
 * 韦恩图操作
 *
 * @author zifang
 */
public class Collections {

    private static final Logger log = LoggerFactory.getLogger(Collections.class);

    /**
     * 求多个集合的并集（去重）
     *
     * @param collection 可变数量的集合
     * @param <E>        泛型元素类型
     * @return 并集结果集合
     */
    public static <E> Collection<E> union(Collection<E>... collection) {
        Collection<E> base = new ArrayList<>();
        for (Collection<E> collectionElement : collection) {
            base.addAll(new ArrayList<>(collectionElement));
        }
        base = base.stream().distinct().collect(Collectors.toList());
        return base;
    }

    /**
     * 求两个集合的交集（仅保留两者都存在的元素）
     *
     * @param collection1 第一个集合
     * @param collection2 第二个集合
     * @param <E>         泛型元素类型
     * @return 交集结果集合
     */
    public static <E> Collection<E> retain(Collection<E> collection1, Collection<E> collection2) {
        Collection<E> left = new ArrayList<>(collection1);
        Collection<E> right = new ArrayList<>(collection2);
        left.retainAll(right);
        return left;
    }

    /**
     * 判断集合是否为空
     *
     * @param collection 参数
     * @return boolean
     */
    public static boolean isEmptyCollection(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 判断集合是否不为空
     *
     * @param collection 参数
     * @return boolean
     */
    public static boolean isNotEmptyCollection(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * 判断map集合是否不为空
     *
     * @param map 参数
     * @return boolean
     */
    public static boolean isNotEmptyMap(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * 判断map集合是否为空
     *
     * @param map 参数
     * @return boolean
     */
    public static boolean isEmptyMap(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 去除列表中的重复元素
     *
     * @param list 待处理的列表
     * @param <T>  泛型元素类型
     * @return 去重后的列表；若输入为 null 或空列表，则返回空列表
     */
    public static <T> List<T> removeDuplicate(List<T> list) {
        if (list == null || list.size() == 0) {
            log.error("list is empty or is null");
            return new ArrayList<>();
        }
        return list.stream().distinct().collect(Collectors.toList());

    }

    /**
     * 求两个列表的交集
     *
     * @param list1 第一个列表
     * @param list2 第二个列表
     * @param <T>   泛型元素类型
     * @return 交集列表；若任一列表为空或 null，则返回空列表
     */
    public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        if (Checker.valid(list1, list2)) {
            Set<T> set = new HashSet<>(list1);
            set.retainAll(list2);
            return new ArrayList<>(set);
        }
        return new ArrayList<>();
    }

    /**
     * 求两个集合的交集
     *
     * @param set1 第一个集合
     * @param set2 第二个集合
     * @param <T>  泛型元素类型
     * @return 交集集合；若任一集合为空或 null，则返回空集合
     */
    public static <T> Set<T> intersection(Collection<T> set1, Collection<T> set2) {
        if (Checker.valid(set1, set2)) {
            List<T> list = new ArrayList<>(set1);
            list.retainAll(set2);
            return new HashSet<>(list);
        }
        return new HashSet<>();
    }

    /**
     * 求两个 Map 的键交集
     *
     * @param map1 第一个 Map
     * @param map2 第二个 Map
     * @param <K>  键的泛型类型
     * @param <V>  值的泛型类型
     * @return 仅包含两个 Map 键交集的 Map；若任一 Map 为空或 null，则返回空 Map
     */
    public static <K, V> Map<K, V> intersection(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> map = new HashMap<>(map1.size());
        if (Checker.valid(map1, map2)) {
            Set<K> setkey1 = new HashSet<>(map1.keySet());
            Set<K> setkey2 = new HashSet<>(map2.keySet());
            setkey1.retainAll(setkey2);
            for (K k : setkey1) {
                map.put(k, map1.get(k));
            }
        }
        return map;
    }

    /**
     * 求两个列表的并集
     *
     * @param list1 第一个列表
     * @param list2 第二个列表
     * @param <T>   泛型元素类型
     * @return 并集列表（不去重）
     */
    public static <T> List<T> unicon(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>();
        list.addAll(list1);
        list.addAll(list2);
        return list;
    }

    /**
     * 求两个集合的并集
     *
     * @param set1 第一个集合
     * @param set2 第二个集合
     * @param <T>  泛型元素类型
     * @return 并集集合（修改并返回 set1）
     */
    public static <T> Set<T> unicon(Set<T> set1, Set<T> set2) {
        set1.addAll(set2);
        return set1;
    }

    /**
     * 求两个队列的并集
     *
     * @param queue1 第一个队列
     * @param queue2 第二个队列
     * @param <T>    泛型元素类型
     * @return 并集队列（修改并返回 queue1）
     */
    public static <T> Queue<T> unicon(Queue<T> queue1, Queue<T> queue2) {
        queue1.addAll(queue2);
        return queue1;
    }

    /**
     * 求两个 Map 的并集
     *
     * @param map1 第一个 Map
     * @param map2 第二个 Map
     * @param <K>  键的泛型类型
     * @param <V>  值的泛型类型
     * @return 并集 Map
     */
    public static <K, V> Map<K, V> unicon(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> map = new HashMap<>(map1.size() + map2.size());
        map.putAll(map1);
        map.putAll(map2);
        return map;
    }


    /**
     * 求两个列表的差集（list1 - list2）
     *
     * @param list1 被减的列表
     * @param list2 减去的列表
     * @param <T>   泛型元素类型
     * @return 差集列表；从 list1 中移除 list2 包含的元素
     */
    public static <T> List<T> subtract(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>(list1.size() + list2.size());
        if (Checker.valid(list1)) {
            list.addAll(list1);
            list.removeAll(list2);
        }
        return list;
    }

    /**
     * 求两个集合的差集（set1 - set2）
     *
     * @param set1 被减的集合
     * @param set2 减去的集合
     * @param <T>  泛型元素类型
     * @return 差集集合；从 set1 中移除 set2 包含的元素
     */
    public static <T> Set<T> subtract(Set<T> set1, Set<T> set2) {
        Set<T> set = new HashSet<>(set1.size() + set2.size());
        if (Checker.valid(set1)) {
            set.addAll(set1);
            set.removeAll(set2);
        }
        return set;
    }

    /**
     * 求两个队列的差集（queue1 - queue2）
     *
     * @param queue1 被减的队列
     * @param queue2 减去的队列
     * @param <T>    泛型元素类型
     * @return 差集队列；从 queue1 中移除 queue2 包含的元素
     */
    public static <T> Queue<T> subtract(Queue<T> queue1, Queue<T> queue2) {
        Queue<T> queue = new LinkedList<>();
        if (Checker.valid(queue1)) {
            queue.addAll(queue1);
            queue.removeAll(queue2);
        }
        return queue;
    }

    /**
     * 求两个 Map 的差集（map1 - map2）
     *
     * @param map1 被减的 Map
     * @param map2 减去的 Map
     * @param <K>  键的泛型类型
     * @param <V>  值的泛型类型
     * @return 差集 Map；只保留 map1 中键不在 map2 中的键值对
     */
    public static <K, V> Map<K, V> subtract(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> map = new HashMap<>(map1.size() + map2.size());
        if (Checker.valid(map1, map2)) {
            Set<K> setkey1 = new HashSet<>(map1.keySet());
            Set<K> setkey2 = new HashSet<>(map2.keySet());
            for (K k : setkey2) {
                setkey1.remove(k);
            }
            for (K k : setkey1) {
                map.put(k, map1.get(k));
            }
        }
        return map;

    }

    /**
     * 将集合中的元素以指定分隔符连接成字符串
     *
     * @param collection 待连接的集合
     * @param separator  元素之间的分隔符
     * @param <T>        泛型元素类型
     * @return 连接后的字符串；末尾不包含分隔符
     */
    public static <T> String join(Collection<T> collection, String separator) {
        StringBuilder sb = new StringBuilder();
        for (T t : collection) {
            sb.append(t.toString()).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }


    /**
     * 将 Map 中的键值对以指定分隔符连接成字符串
     *
     * @param map        待连接的 Map
     * @param separator  键值对之间的分隔符（如 "，"）
     * @param separator1 键与值之间的分隔符（如 "="）
     * @param <K>        键的泛型类型
     * @param <V>        值的泛型类型
     * @return 连接后的字符串；末尾不包含键值对分隔符
     */
    public static <K, V> String join(Map<K, V> map, String separator, String separator1) {
        if (map == null || map.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(separator1)
                    .append(entry.getValue()).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - separator.length());
    }

    /**
     * 将map的key以separator链接并以字符串的形式返回
     *
     * @param map       map
     * @param separator 连接符
     * @param <K>       泛型
     * @param <V>       泛型
     * @return 字符串
     */
    public static <K, V> String keyJoin(Map<K, V> map, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append(entry.getKey()).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - separator.length());
    }

    /**
     * 将map的value以separator链接并以字符串的形式返回
     *
     * @param map       map
     * @param separator 连接符
     * @param <K>       泛型
     * @param <V>       泛型
     * @return 字符串
     */
    public static <K, V> String valueJoin(Map<K, V> map, String separator) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            sb.append(entry.getValue()).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - separator.length());
    }

}