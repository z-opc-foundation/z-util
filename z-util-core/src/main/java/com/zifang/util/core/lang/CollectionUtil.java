package com.zifang.util.core.lang;

import com.zifang.util.core.lang.primitive.ByteUtil;
import com.zifang.util.core.lang.primitive.IntegerUtil;
import com.zifang.util.core.lang.primitive.LongUtil;
import com.zifang.util.core.lang.primitive.ShortUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zifang.util.core.lang.MapUtil.MAX_POWER_OF_TWO;

/**
 * @author: zifang
 * @time: 2021-10-25 18:59:00
 * @description: collection util
 * @version: JDK 1.8
 */
public class CollectionUtil {

    /**
     * isEmpty方法。
     * * @param collection CollectionT类型参数
     *
     * @return static <T> boolean类型返回值
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * isNotEmpty方法。
     * * @param collection CollectionT类型参数
     *
     * @return static <T> boolean类型返回值
     */
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    /**
     * newHashSet方法。
     * * @param expectedSize int类型参数
     *
     * @return static <E> HashSet<E>类型返回值
     */
    public static <E> HashSet<E> newHashSet(int expectedSize) {
        return new HashSet<E>(MapUtil.capacity(expectedSize));
    }

    /**
     * newHashSet方法。
     *
     * @return static <E> HashSet<E>类型返回值
     */
    public static <E> HashSet<E> newHashSet() {
        return newHashSet(16);
    }

    /**
     * containsInstance方法。
     * * @param collection CollectionT类型参数
     *
     * @param element Object类型参数
     * @return static <T> boolean类型返回值
     */
    public static <T> boolean containsInstance(Collection<T> collection, Object element) {
        if (collection != null) {
            for (Object candidate : collection) {
                if (candidate == element) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @author: zifang
     * @description: compare two collection
     * @time: 2021/12/18 16:46
     * @params: [source, target] request
     * @return: boolean response
     */
    public static <T> boolean equals(Collection<T> source, Collection<T> target) {
        return equals(source, target, Collection::hashCode);
    }

    /**
     * @author: zifang
     * @description: compare two collection use way of compute hashCode
     * @time: 2021/12/18 16:46
     * @params: [source, target, callback] request
     * @return: boolean response
     */
    public static <T> boolean equals(Collection<T> source, Collection<T> target,
                                     HashCallback<T> callback) {
        if (isEmpty(source) && isNotEmpty(target)) {
            return false;
        }
        if (isNotEmpty(source) && isEmpty(target)) {
            return false;
        }
        if (null == source && null == target) {
            return true;
        }
        if (source.size() != target.size()) {
            return false;
        }

        return callback.computeHashCode(source) == callback.computeHashCode(target);
    }

    /**
     * containsAny方法。
     * * @param source CollectionT类型参数
     *
     * @param candidates CollectionT类型参数
     * @return static <T> boolean类型返回值
     */
    public static <T> boolean containsAny(Collection<T> source, Collection<T> candidates) {
        return findFirstMatch(source, candidates) != null;
    }

    /**
     * random方法。
     * * @param list ListT类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T random(List<T> list) {
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(index);
    }

    /**
     * newArrayList方法。
     * * @param elements E...类型参数
     *
     * @return static <E> ArrayList<E>类型返回值
     */
    public static <E> ArrayList<E> newArrayList(E... elements) {
        if (null == elements) {
            throw new NullPointerException();
        }
        int arraySize = elements.length;
        ArrayList<E> list = new ArrayList<>(suitableCapacity(arraySize));
        Collections.addAll(list, elements);
        return list;
    }

    /**
     * newListArray方法。
     * * @param elements E[]类型参数
     *
     * @return static <E> ArrayList<E>类型返回值
     */
    public static <E> ArrayList<E> newListArray(E[] elements) {
        if (null == elements) {
            throw new NullPointerException();
        }
        return newArrayList(elements);
    }

    /**
     * suitableCapacity方法。
     * * @param arraySize int类型参数
     *
     * @return static int类型返回值
     */
    public static int suitableCapacity(int arraySize) {
        return IntegerUtil.saturatedCast(5 + arraySize + arraySize / 10);
    }


    /**
     * findFirstMatch方法。
     * * @param source CollectionSOURCE类型参数
     *
     * @param candidates CollectionE类型参数
     * @return static <SOURCE, E> E类型返回值
     */
    public static <SOURCE, E> E findFirstMatch(Collection<SOURCE> source, Collection<E> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return null;
        }
        for (Object candidate : candidates) {
            if (source.contains(candidate)) {
                return (E) candidate;
            }
        }
        return null;
    }


    /**
     * findValueOfType方法。
     * * @param collection CollectionT类型参数
     *
     * @param type ClassT类型参数
     * @return static <T> T类型返回值
     */
    public static <T> T findValueOfType(Collection<T> collection, Class<T> type) {
        if (isEmpty(collection)) {
            return null;
        }
        T value = null;
        for (Object element : collection) {
            if (type == null || type.isInstance(element)) {
                if (value != null) {
                    // More than one value found... no clear single value.
                    return null;
                }
                value = (T) element;
            }
        }
        return value;
    }


    /**
     * hasUniqueObject方法。
     * * @param collection CollectionT类型参数
     *
     * @return static <T> boolean类型返回值
     */
    public static <T> boolean hasUniqueObject(Collection<T> collection) {
        if (isEmpty(collection)) {
            return false;
        }
        boolean hasCandidate = false;
        Object candidate = null;
        for (Object elem : collection) {
            if (!hasCandidate) {
                hasCandidate = true;
                candidate = elem;
            } else if (candidate != elem) {
                return false;
            }
        }
        return true;
    }

    /**
     * findCommonElementType方法。
     * * @param collection CollectionT类型参数
     *
     * @return static <T> Class<?>类型返回值
     */
    public static <T> Class<?> findCommonElementType(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        Class<?> candidate = null;
        for (Object val : collection) {
            if (val != null) {
                if (candidate == null) {
                    candidate = val.getClass();
                } else if (candidate != val.getClass()) {
                    return null;
                }
            }
        }
        return candidate;
    }


    /**
     * firstElement方法。
     * * @param set SetT类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T firstElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).first();
        }

        Iterator<T> it = set.iterator();
        T first = null;
        if (it.hasNext()) {
            first = it.next();
        }
        return first;
    }


    /**
     * firstElement方法。
     * * @param list ListT类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T firstElement(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }


    /**
     * lastElement方法。
     * * @param set SetT类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T lastElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).last();
        }

        // Full iteration necessary...
        Iterator<T> it = set.iterator();
        T last = null;
        while (it.hasNext()) {
            last = it.next();
        }
        return last;
    }


    /**
     * lastElement方法。
     * * @param list ListT类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T lastElement(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * toArray方法。
     * * @param enumeration EnumerationE类型参数
     *
     * @param array A[]类型参数
     * @return static <A, E extends A> A[]类型返回值
     */
    public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
        ArrayList<A> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }

    /**
     * toIterator方法。
     * * @param enumeration EnumerationE类型参数
     *
     * @return static <E> Iterator<E>类型返回值
     */
    public static <E> Iterator<E> toIterator(Enumeration<E> enumeration) {
        return (enumeration != null ? new EnumerationIterator<>(enumeration)
                : Collections.emptyIterator());
    }

    /**
     * mergePropertiesIntoMap方法。
     * * @param props Properties类型参数
     *
     * @param map MapK,类型参数
     * @return static <K, V> void类型返回值
     */
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                Object value = props.get(key);
                if (value == null) {
                    // Allow for defaults fallback or potentially overridden accessor...
                    value = props.getProperty(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }

    /**
     * newHashMap方法。
     * * @param expectedSize int类型参数
     *
     * @return static <K, V> HashMap<K, V>类型返回值
     */
    public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(capacity(expectedSize));
    }

    /**
     * capacity方法。
     * * @param expectedSize int类型参数
     *
     * @return static int类型返回值
     */
    protected static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            checkNonNegative(expectedSize, "expectedSize");
            return expectedSize + 1;
        }
        if (expectedSize < MAX_POWER_OF_TWO) {
            // This is the calculation used in JDK8 to resize when a putAll
            // happens; it seems to be the most conservative calculation we
            // can make.  0.75 is the default load factor.
            return (int) ((float) expectedSize / 0.75F + 1.0F);
        }
        return Integer.MAX_VALUE;
    }

    private static int checkNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
        }
        return value;
    }

    /**
     * isEmpty方法。
     * * @param map MapK,类型参数
     *
     * @return static <K, V> boolean类型返回值
     */
    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * isNotEmpty方法。
     * * @param map MapK,类型参数
     *
     * @return static <K, V> boolean类型返回值
     */
    public static <K, V> boolean isNotEmpty(Map<K, V> map) {
        return !isEmpty(map);
    }

    /**
     * parseValue方法。
     * * @param map MapK,类型参数
     *
     * @param key K类型参数
     * @return static <K, V> V类型返回值
     */
    public static <K, V> V parseValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return map.get(key);
    }

    /**
     * parseStringValue方法。
     * * @param map MapK,类型参数
     *
     * @param key K类型参数
     * @return static <K, V> String类型返回值
     */
    public static <K, V> String parseStringValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return StringUtil.parseString(map.get(key));
    }

    /**
     * parseByteValue方法。
     * * @param map MapK,类型参数
     *
     * @param key K类型参数
     * @return static <K, V> Byte类型返回值
     */
    public static <K, V> Byte parseByteValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return ByteUtil.parseByte(map.get(key));
    }

    /**
     * parseShortValue方法。
     * * @param map MapK,类型参数
     *
     * @param key K类型参数
     * @return static <K, V> Short类型返回值
     */
    public static <K, V> Short parseShortValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return ShortUtil.parseShort(map.get(key));
    }

    /**
     * parseIntegerValue方法。
     * * @param map MapK,类型参数
     *
     * @param key K类型参数
     * @return static <K, V> Integer类型返回值
     */
    public static <K, V> Integer parseIntegerValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return IntegerUtil.parseInteger(map.get(key));
    }

    /**
     * parseLongValue方法。
     * * @param map MapK,类型参数
     *
     * @param key K类型参数
     * @return static <K, V> Long类型返回值
     */
    public static <K, V> Long parseLongValue(Map<K, V> map, K key) {
        if (isEmpty(map)) {
            return null;
        }
        return LongUtil.parseLong(map.get(key));
    }

    /**
     * parseValueOrDefault方法。
     * * @param map MapK,类型参数
     *
     * @param key          K类型参数
     * @param defaultValue V类型参数
     * @return static <K, V> V类型返回值
     */
    public static <K, V> V parseValueOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (isEmpty(map)) {
            return defaultValue;
        }
        return map.get(key);
    }

    /**
     * distinctByKey 的 null 键哨兵，ConcurrentHashMap 的键集合不允许 null，以哨兵代替参与去重
     */
    private static final Object NULL_KEY = new Object();

    /**
     * groupingByNullSafe方法。支持分组键为 null 的分组收集器。
     * JDK 标准的 Collectors.groupingBy 在分组键为 null 时抛出 NullPointerException，
     * 本方法将 null 键与普通键一样处理，归入同一分组。
     *
     * @param classifier 分组键提取函数，允许返回 null
     * @param <T>        流元素类型
     * @param <K>        分组键类型
     * @return 分组结果收集器，键为分组键（可为 null），值为该组元素列表
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingByNullSafe(Function<? super T, ? extends K> classifier) {
        return Collectors.toMap(classifier, Collections::singletonList,
                (List<T> oldList, List<T> newElement) -> {
                    List<T> newList = new ArrayList<>(oldList.size() + 1);
                    newList.addAll(oldList);
                    newList.addAll(newElement);
                    return newList;
                });
    }

    /**
     * group方法。按比较器定义的等价关系将元素分组。
     * 与 groupingBy 不同，无需提取分组键，只要比较器判定两元素相等（返回 0）即归入同组，
     * 分组与组内元素均保持原列表中的出现顺序。
     *
     * @param data       元素列表，null 或空列表返回空结果
     * @param comparator 比较器
     * @param <T>        元素类型
     * @return 分组结果，每组为等价元素列表
     */
    public static <T> List<List<T>> group(List<T> data, Comparator<? super T> comparator) {
        List<List<T>> result = new ArrayList<>();
        if (isEmpty(data)) {
            return result;
        }
        for (T element : data) {
            boolean merged = false;
            for (List<T> group : result) {
                if (comparator.compare(element, group.get(0)) == 0) {
                    group.add(element);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                List<T> newGroup = new ArrayList<>();
                newGroup.add(element);
                result.add(newGroup);
            }
        }
        return result;
    }

    /**
     * distinctByKey方法。构造按提取键去重的 Predicate，配合 stream 的 filter 使用。
     * 与 stream 的 distinct 不同，可按元素的业务键去重并保留首次出现的元素；
     * 键为 null 的元素视为同一键，保留首个。
     *
     * @param keyExtractor 去重键提取函数
     * @param <T>          元素类型
     * @return 有状态的去重 Predicate（仅适用于单次顺序流，勿复用）
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> {
            Object key = keyExtractor.apply(t);
            return seen.add(key == null ? NULL_KEY : key);
        };
    }

    /**
     * page方法。对列表做内存分页，返回第 pageNum 页（页码从 1 开始）的 pageSize 条元素。
     * 页码越界、集合为空或每页条数非法时返回空列表；返回结果为新列表，修改不影响原集合。
     *
     * @param list     待分页的列表，为 null 或空时返回空列表
     * @param pageNum  页码，从 1 开始，小于 1 时按第 1 页处理
     * @param pageSize 每页条数，小于等于 0 时返回空列表
     * @param <T>      元素类型
     * @return 当前页元素组成的新列表
     */
    public static <T> List<T> page(List<T> list, int pageNum, int pageSize) {
        if (isEmpty(list) || pageSize <= 0) {
            return new ArrayList<>();
        }
        int fromIndex = (Math.max(pageNum, 1) - 1) * pageSize;
        int total = list.size();
        if (fromIndex >= total) {
            return new ArrayList<>();
        }
        int toIndex = Math.min(fromIndex + pageSize, total);
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }

    /**
     * partition方法。将列表按固定大小切分为多个子列表（整表切分）。
     * 最后一批为剩余元素；返回结果与各子列表均为新列表，修改不影响原集合。
     *
     * @param list 待切分的列表，为 null 或空时返回空列表
     * @param size 每个子列表的大小，小于等于 0 时抛出 IllegalArgumentException
     * @param <T>  元素类型
     * @return 子列表组成的新列表
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive: " + size);
        }
        List<List<T>> partitions = new ArrayList<>();
        if (isEmpty(list)) {
            return partitions;
        }
        int total = list.size();
        for (int i = 0; i < total; i += size) {
            partitions.add(new ArrayList<>(list.subList(i, Math.min(i + size, total))));
        }
        return partitions;
    }

    /**
     * partitionAsStream方法。将列表按固定大小切分并以流的形式返回各批。
     *
     * @param list 待切分的列表，为 null 或空时返回空流
     * @param size 每批的大小，小于等于 0 时抛出 IllegalArgumentException
     * @param <T>  元素类型
     * @return 各批组成的流
     */
    public static <T> Stream<List<T>> partitionAsStream(List<T> list, int size) {
        return partition(list, size).stream();
    }

    /**
     * processInBatches方法。将列表按固定大小分批交给处理器处理，返回已处理的元素个数。
     * 处理器返回 false 时中断后续批次；已处理计数为当前批之前累计的元素个数。
     *
     * @param list      待处理的列表，为 null 或空时返回 0
     * @param size      每批的大小，小于等于 0 时抛出 IllegalArgumentException
     * @param processor 批处理器，为 null 时抛出 IllegalArgumentException
     * @param <T>       元素类型
     * @return 已处理的元素个数
     */
    public static <T> int processInBatches(List<T> list, int size, BatchProcessor<T> processor) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive: " + size);
        }
        if (processor == null) {
            throw new IllegalArgumentException("processor must not be null");
        }
        if (isEmpty(list)) {
            return 0;
        }
        int total = list.size();
        int processedCount = 0;
        for (int i = 0; i < total; i += size) {
            List<T> batch = list.subList(i, Math.min(i + size, total));
            if (!processor.process(batch, processedCount)) {
                break;
            }
            processedCount += batch.size();
        }
        return processedCount;
    }

    /**
     * batchCount方法。计算按固定批大小组批时的批数（向上取整）。
     *
     * @param totalSize 元素总个数，小于等于 0 时返回 0
     * @param batchSize 批大小，小于等于 0 时抛出 IllegalArgumentException
     * @return 批数
     */
    public static int batchCount(int totalSize, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive: " + batchSize);
        }
        if (totalSize <= 0) {
            return 0;
        }
        return (totalSize + batchSize - 1) / batchSize;
    }

    /**
     * BatchProcessor接口。批处理器，process 返回 false 时中断后续批次。
     *
     * @param <T> 元素类型
     */
    @FunctionalInterface
    public interface BatchProcessor<T> {

        /**
         * process方法。处理单个批次。
         *
         * @param batch         当前批元素
         * @param processedCount 当前批之前累计已处理的元素个数
         * @return boolean类型返回值，返回 false 时中断后续批次
         */
        boolean process(List<T> batch, int processedCount);
    }


    @FunctionalInterface
/**
 * HashCallback接口。
 */
    public interface HashCallback<T> {

        int computeHashCode(Collection<T> collection);

    }

    private static class EnumerationIterator<E> implements Iterator<E> {

        private final Enumeration<E> enumeration;

        /**
         * EnumerationIterator方法。
         * * @param enumeration EnumerationE类型参数
         */
        public EnumerationIterator(Enumeration<E> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        /**
         * hasNext方法。
         * @return boolean类型返回值
         */
        public boolean hasNext() {
            return this.enumeration.hasMoreElements();
        }

        @Override
        /**
         * next方法。
         * @return E类型返回值
         */
        public E next() {
            return this.enumeration.nextElement();
        }

        @Override
        /**
         * remove方法。
         */
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported");
        }
    }

}
