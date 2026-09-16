package com.zifang.util.core.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 集合工具（CollectionUtil）的扩展，提供分批处理、findFirst 等常用但 CollectionUtil 尚未覆盖的操作。
 *
 * @author zifang
 */
public final class CollectionOps {

    private static final Logger log = LoggerFactory.getLogger(CollectionOps.class);

    private CollectionOps() {
    }

    /**
     * 把集合切成指定大小的批量（用于批量入库、批量发送等场景）。
     *
     * @param source    源集合
     * @param batchSize 每批大小（必须 &gt; 0）
     * @param <T>       元素类型
     * @return 切分后的批量列表（按源集合顺序）
     */
    public static <T> List<List<T>> partition(Collection<T> source, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be > 0");
        }
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        List<List<T>> result = new ArrayList<>((source.size() + batchSize - 1) / batchSize);
        List<T> current = new ArrayList<>(batchSize);
        for (T item : source) {
            current.add(item);
            if (current.size() == batchSize) {
                result.add(current);
                current = new ArrayList<>(batchSize);
            }
        }
        if (!current.isEmpty()) {
            result.add(current);
        }
        return result;
    }

    /**
     * 找到第一个匹配的元素，未找到返回 null。
     */
    public static <T> T findFirst(Collection<T> source, Predicate<T> predicate) {
        if (source == null || predicate == null) return null;
        for (T item : source) {
            if (predicate.test(item)) return item;
        }
        return null;
    }

    /**
     * 返回与 predicate 匹配的元素数量。
     */
    public static <T> long count(Collection<T> source, Predicate<T> predicate) {
        if (source == null || predicate == null) return 0L;
        long n = 0;
        for (T item : source) {
            if (predicate.test(item)) n++;
        }
        return n;
    }

    /**
     * 两个集合是否有任一共同元素（null 安全）。
     */
    public static <T> boolean hasIntersection(Collection<T> a, Collection<T> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return false;
        Collection<T> smaller = a.size() <= b.size() ? a : b;
        Collection<T> larger = smaller == a ? b : a;
        for (T item : smaller) {
            if (larger.contains(item)) return true;
        }
        return false;
    }

    /**
     * 首个非 null 元素。
     */
    @SafeVarargs
    public static <T> T firstNonNull(T... items) {
        if (items == null) return null;
        for (T item : items) {
            if (item != null) return item;
        }
        return null;
    }

    /**
     * 是否所有元素都不为 null。
     */
    public static boolean noNulls(Collection<?> source) {
        if (source == null || source.isEmpty()) return true;
        for (Object o : source) {
            if (o == null) return false;
        }
        return true;
    }

    /**
     * 便捷 logging wrapper：debug 打印集合大小（避免大集合 toString 拖慢日志）。
     */
    public static void debugSize(String label, Collection<?> source) {
        if (log.isDebugEnabled()) {
            log.debug("{} size={}", label, source == null ? 0 : source.size());
        }
    }

    /**
     * 两个集合的内容是否完全一致（顺序无关）。
     */
    public static <T> boolean sameElements(Collection<T> a, Collection<T> b) {
        if (a == null || b == null) return a == b;
        if (a.size() != b.size()) return false;
        // 复制 b 用于 contains 检查（避免修改原集合）
        java.util.Set<T> bs = new java.util.HashSet<>(b);
        for (T item : a) {
            if (!bs.remove(item)) return false;
        }
        return true;
    }

    /**
     * 安全获取元素（index 越界返回 null）。
     */
    public static <T> T get(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) return null;
        return list.get(index);
    }

    /**
     * null-safe 比较：两个集合用 Objects.equals 比较。
     */
    public static boolean equalsIgnoreOrder(Collection<?> a, Collection<?> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;
        java.util.Set<Object> bs = new java.util.HashSet<>(b);
        for (Object item : a) {
            if (!bs.remove(item)) {
                if (!b.contains(item)) return false;
            }
        }
        return true;
    }
}