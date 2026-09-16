package com.zifang.util.core.lang.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 集合的流式变换工具类
 * <p>
 * 提供null安全的集合快捷变换方法：集合转Map、映射、过滤、分组、拼接等。
 * 所有方法都会先剔除源集合中的null元素；源集合为null或空时返回空结果，
 * 不会抛出NullPointerException。
 *
 * @author zifang
 */
public class StreamUtil {

    /**
     * 集合转Map，元素自身作为值
     *
     * @param collection 待转集合
     * @param keyMapper  key函数
     * @param <T>        集合元素类型
     * @param <K>        key类型
     * @return Map；key冲突时后值覆盖前值
     */
    public static <T, K> Map<K, T> toMap(Collection<T> collection, Function<T, K> keyMapper) {
        return toMap(collection, keyMapper, Function.identity());
    }

    /**
     * 集合转Map
     *
     * @param collection  待转集合
     * @param keyMapper   key函数
     * @param valueMapper value函数
     * @param <T>         集合元素类型
     * @param <K>         key类型
     * @param <V>         value类型
     * @return Map；key冲突时后值覆盖前值
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> collection, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(keyMapper, valueMapper, (k1, k2) -> k2));
    }

    /**
     * 映射为List
     *
     * @param collection 待转集合
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @param <R>        目标元素类型
     * @return 映射后的List（映射结果中的null会保留）
     */
    public static <T, R> List<R> mapToList(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 映射为去重List，同时剔除映射结果中的null
     *
     * @param collection 待转集合
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @param <R>        目标元素类型
     * @return 映射去重后的List
     */
    public static <T, R> List<R> mapToDistinctList(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 映射后以逗号拼接
     *
     * @param collection 待转集合
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @return 拼接后的字符串，空集合返回空字符串
     */
    public static <T> String mapToJoin(Collection<T> collection, Function<T, String> mapper) {
        return mapToJoin(collection, mapper, ",");
    }

    /**
     * 映射后以指定分隔符拼接
     *
     * @param collection 待转集合
     * @param mapper     映射函数
     * @param delimiter  分隔符
     * @param <T>        原元素类型
     * @return 拼接后的字符串，空集合返回空字符串
     */
    public static <T> String mapToJoin(Collection<T> collection, Function<T, String> mapper, String delimiter) {
        if (isEmpty(collection)) {
            return "";
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * 映射为List，同时剔除映射结果中的null
     *
     * @param collection 待转集合
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @param <R>        目标元素类型
     * @return 映射后的List，不含null
     */
    public static <T, R> List<R> mapToListNonnull(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 过滤后映射为List
     *
     * @param collection 待转集合
     * @param filter     过滤条件
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @param <R>        目标元素类型
     * @return 过滤映射后的List
     */
    public static <T, R> List<R> mapToList(Collection<T> collection, Predicate<T> filter, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * 过滤后映射为Set
     *
     * @param collection 待转集合
     * @param filter     过滤条件
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @param <R>        目标元素类型
     * @return 过滤映射后的Set
     */
    public static <T, R> Set<R> mapToSet(Collection<T> collection, Predicate<T> filter, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptySet();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toSet());
    }

    /**
     * 映射为Set
     *
     * @param collection 待转集合
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @param <R>        目标元素类型
     * @return 映射后的Set
     */
    public static <T, R> Set<R> mapToSet(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptySet();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .collect(Collectors.toSet());
    }

    /**
     * 按key分组，元素自身作为值
     *
     * @param collection 待分组集合
     * @param keyMapper  分组key函数
     * @param <T>        集合元素类型
     * @param <K>        分组key类型
     * @return 分组后的Map
     */
    public static <T, K> Map<K, List<T>> listToGroup(Collection<T> collection, Function<T, K> keyMapper) {
        if (isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(r -> Objects.nonNull(r) && Objects.nonNull(keyMapper.apply(r)))
                .collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * 按key分组后映射值
     *
     * @param collection  待分组集合
     * @param keyMapper   分组key函数
     * @param valueMapper 值映射函数
     * @param <T>         集合元素类型
     * @param <K>         分组key类型
     * @param <V>         值类型
     * @return 分组映射后的Map
     */
    public static <T, K, V> Map<K, List<V>> listToGroup(Collection<T> collection, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(r -> Objects.nonNull(r) && Objects.nonNull(keyMapper.apply(r)))
                .collect(Collectors.groupingBy(keyMapper, Collectors.mapping(valueMapper, Collectors.toList())));
    }

    /**
     * 按key分组后将值拼接为字符串，保持首次出现的key顺序
     *
     * @param collection  待分组集合
     * @param keyMapper   分组key函数
     * @param valueMapper 值映射函数，映射结果以逗号拼接
     * @param <T>         集合元素类型
     * @param <K>         分组key类型
     * @return 分组拼接后的Map
     */
    public static <T, K> Map<K, String> listToGroupJoin(Collection<T> collection, Function<T, K> keyMapper, Function<T, String> valueMapper) {
        if (isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(r -> Objects.nonNull(r) && Objects.nonNull(keyMapper.apply(r)))
                .collect(Collectors.groupingBy(keyMapper, LinkedHashMap::new,
                        Collectors.mapping(valueMapper, Collectors.joining(","))));
    }

    /**
     * 按key分组，允许key为null的元素参与分组（归入null键组）
     * <p>
     * 与 {@link #listToGroup(Collection, Function)} 不同：后者会剔除key为null的元素，
     * 本方法将其保留在 {@code null} 键对应的分组中；元素为null时跳过。
     *
     * @param collection 待分组集合
     * @param keyMapper  分组key函数，返回null时归入null键组
     * @param <T>        集合元素类型
     * @param <K>        分组key类型
     * @return 分组后的Map（保持首次出现的key顺序）
     */
    public static <T, K> Map<K, List<T>> listToGroupWithNullKey(Collection<T> collection, Function<T, K> keyMapper) {
        Map<K, List<T>> result = new LinkedHashMap<>();
        if (isEmpty(collection)) {
            return result;
        }
        for (T item : collection) {
            if (item == null) {
                continue;
            }
            K key = keyMapper.apply(item);
            result.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return result;
    }

    /**
     * 按比较器进行等价类分组：与已有分组首元素比较结果为0即归入该组
     * <p>
     * 顺序遍历元素，依次与各已有分组的首元素比较，{@code compare==0} 则归入该组，
     * 否则新建分组；适用于按自定义等价关系聚合的场景。
     *
     * @param collection 待分组集合，null或空时返回空列表
     * @param comparator 等价关系比较器
     * @param <T>        元素类型
     * @return 分组后的列表（保持元素首次出现顺序）
     */
    public static <T> List<List<T>> group(Collection<T> collection, Comparator<? super T> comparator) {
        List<List<T>> result = new ArrayList<>();
        if (isEmpty(collection)) {
            return result;
        }
        for (T item : collection) {
            if (item == null) {
                continue;
            }
            boolean grouped = false;
            for (List<T> group : result) {
                if (comparator.compare(item, group.get(0)) == 0) {
                    group.add(item);
                    grouped = true;
                    break;
                }
            }
            if (!grouped) {
                List<T> newGroup = new ArrayList<>();
                newGroup.add(item);
                result.add(newGroup);
            }
        }
        return result;
    }

    /**
     * 剔除集合中的null元素，返回新列表（不修改入参集合）
     *
     * @param collection 原集合
     * @param <T>        元素类型
     * @return 剔除null后的新列表；入参为null或空时返回空列表
     */
    public static <T> List<T> removeNull(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (T item : collection) {
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 过滤集合，剔除null元素后按条件过滤
     *
     * @param collection 待过滤集合
     * @param filter     过滤条件
     * @param <T>        元素类型
     * @return 过滤后的新列表；入参为null或空时返回空列表
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> filter) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * 查找第一个满足条件的元素
     *
     * @param collection 待查找集合
     * @param filter     匹配条件
     * @param <T>        元素类型
     * @return 第一个匹配的元素；无匹配或集合为null/空时返回null
     */
    public static <T> T findAny(Collection<T> collection, Predicate<T> filter) {
        if (isEmpty(collection)) {
            return null;
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .findFirst()
                .orElse(null);
    }

    /**
     * 去重，剔除null元素后去除重复项
     *
     * @param collection 待去重集合
     * @param <T>        元素类型
     * @return 去重后的新列表；入参为null或空时返回空列表
     */
    public static <T> List<T> distinct(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 过滤后映射为去重List，同时剔除映射结果中的null
     *
     * @param collection 待转集合
     * @param filter     过滤条件
     * @param mapper     映射函数
     * @param <T>        原元素类型
     * @param <R>        目标元素类型
     * @return 过滤映射去重后的List
     */
    public static <T, R> List<R> mapToDistinctList(Collection<T> collection, Predicate<T> filter, Function<T, R> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .map(mapper)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 过滤后转Map
     *
     * @param collection  待转集合
     * @param filter      过滤条件
     * @param keyMapper   key函数
     * @param valueMapper value函数
     * @param <T>         集合元素类型
     * @param <K>         key类型
     * @param <V>         value类型
     * @return Map；key冲突时后值覆盖前值
     */
    public static <T, K, V> Map<K, V> toMap(Collection<T> collection, Predicate<T> filter,
                                            Function<T, K> keyMapper, Function<T, V> valueMapper) {
        if (isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(filter)
                .collect(Collectors.toMap(keyMapper, valueMapper, (k1, k2) -> k2));
    }

    /**
     * 从Map中批量取多个key对应的非空值
     *
     * @param map  目标Map
     * @param keys key集合
     * @param <K>  key类型
     * @param <V>  value类型
     * @return 依次取出非null值的列表；map或keys为null时返回空列表
     */
    public static <K, V> List<V> multiGet(Map<K, V> map, Collection<K> keys) {
        List<V> result = new ArrayList<>();
        if (map == null || isEmpty(keys)) {
            return result;
        }
        for (K key : keys) {
            V value = map.get(key);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * 按指定key去重的过滤条件，用于流式filter
     * <p>
     * 例如：{@code list.stream().filter(StreamUtil.distinctByKey(User::getName))}
     * 效果为按name保留首个出现的元素。
     *
     * @param keyExtractor 比较key的提取函数
     * @param <T>          元素类型
     * @return 去重过滤条件
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> {
            Object key = keyExtractor.apply(t);
            // key为null时不过滤，直接保留
            if (key == null) {
                return true;
            }
            return seen.putIfAbsent(key, Boolean.TRUE) == null;
        };
    }

    /**
     * 只保留重复元素的过滤条件（与distinctByKey互补），用于流式filter
     * <p>
     * 例如：{@code list.stream().filter(StreamUtil.distinctNotByKey(User::getName))}
     * 效果为保留name重复出现的后续元素（不含首个）。
     *
     * @param keyExtractor 比较key的提取函数
     * @param <T>          元素类型
     * @return 保留重复项的过滤条件
     */
    public static <T> Predicate<T> distinctNotByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> {
            Object key = keyExtractor.apply(t);
            // key为null时无重复可言，不保留
            if (key == null) {
                return false;
            }
            return seen.putIfAbsent(key, Boolean.TRUE) != null;
        };
    }

    /**
     * 拼接两个集合，返回新列表（不修改入参集合）
     *
     * @param collection1 第一个集合，null视为空
     * @param collection2 第二个集合，null视为空
     * @param <T>         元素类型
     * @return 拼接后的新列表，保留元素原有顺序（含null元素）
     */
    public static <T> List<T> concat(Collection<T> collection1, Collection<T> collection2) {
        List<T> result = new ArrayList<>();
        if (!isEmpty(collection1)) {
            result.addAll(collection1);
        }
        if (!isEmpty(collection2)) {
            result.addAll(collection2);
        }
        return result;
    }

    /**
     * 单元素与集合合并去重，返回新列表（不修改入参集合）
     *
     * @param element   单个元素，null时忽略
     * @param collection 集合，null或空时结果只含element
     * @param <T>       元素类型
     * @return 合并去重后的新列表，集合元素在前、单元素在后
     */
    public static <T> List<T> combine(T element, Collection<T> collection) {
        List<T> result = new ArrayList<>();
        if (!isEmpty(collection)) {
            result.addAll(collection);
        }
        if (element != null && !result.contains(element)) {
            result.add(element);
        }
        return result;
    }

    /**
     * 合并多个集合并去重，返回新列表（不修改入参集合）
     * <p>
     * 依次拼接所有非空集合，剔除null元素后去重（保留首次出现）；
     * 与 {@link #concat(Collection, Collection)} 的区别在于会过滤null并去重。
     *
     * @param collections 待合并的多个集合，null或空集合自动跳过
     * @param <T>         元素类型
     * @return 合并去重后的新列表，保持元素首次出现的顺序
     */
    @SafeVarargs
    public static <T> List<T> mergeDistinct(Collection<T>... collections) {
        List<T> result = new ArrayList<>();
        if (collections == null) {
            return result;
        }
        for (Collection<T> collection : collections) {
            if (isEmpty(collection)) {
                continue;
            }
            for (T item : collection) {
                if (item != null && !result.contains(item)) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * 排序，集合为null或空时不做任何操作
     *
     * @param list       待排序列表
     * @param comparator 比较器
     * @param <T>        元素类型
     */
    public static <T> void sort(List<T> list, java.util.Comparator<T> comparator) {
        if (isEmpty(list)) {
            return;
        }
        list.sort(comparator);
    }

    /**
     * 逗号分隔的字符串转Set，剔除空白项
     *
     * @param str 待拆分字符串
     * @return Set；str为null或空白时返回空Set
     */
    public static Set<String> splitToSet(String str) {
        return splitToSet(str, ",");
    }

    /**
     * 指定分隔符的字符串转Set，剔除空白项
     *
     * @param str       待拆分字符串
     * @param separator 分隔符
     * @return Set
     */
    public static Set<String> splitToSet(String str, String separator) {
        List<String> items = splitToList(str, separator);
        return new HashSet<>(items);
    }

    /**
     * 指定分隔符的字符串转List，剔除空白项
     *
     * @param str       待拆分字符串
     * @param separator 分隔符，按字面量处理（支持正则元字符如"|""."）
     * @return List；str或separator为null/空时返回空List
     */
    public static List<String> splitToList(String str, String separator) {
        if (str == null || str.isEmpty() || separator == null || separator.isEmpty()) {
            return Collections.emptyList();
        }
        String[] tokens = str.split(java.util.regex.Pattern.quote(separator));
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (token != null && !token.trim().isEmpty()) {
                result.add(token);
            }
        }
        return result;
    }

    private static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
