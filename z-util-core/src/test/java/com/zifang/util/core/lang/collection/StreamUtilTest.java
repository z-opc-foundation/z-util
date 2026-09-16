package com.zifang.util.core.lang.collection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * StreamUtilTest类。
 */
public class StreamUtilTest {

    @Test
    /**
     * testToMap方法。
     */
    public void testToMap() {
        List<String> list = Arrays.asList("a", "bb", "ccc");
        // 元素自身作为值
        Map<Integer, String> byLength = StreamUtil.toMap(list, String::length);
        assertEquals("bb", byLength.get(2));
        // key/value映射
        Map<String, Integer> nameToLen = StreamUtil.toMap(list, s -> "k_" + s, String::length);
        assertEquals(Integer.valueOf(3), nameToLen.get("k_ccc"));
        // null元素剔除
        List<String> withNull = new ArrayList<>(Arrays.asList("a", null, "b"));
        assertEquals(2, StreamUtil.toMap(withNull, s -> s).size());
        // key冲突后值覆盖前值
        List<String> dup = Arrays.asList("ab", "cd");
        Map<Integer, String> m = StreamUtil.toMap(dup, String::length);
        assertEquals("cd", m.get(2));
        // 空集合
        assertTrue(StreamUtil.toMap(new ArrayList<String>(), s -> s).isEmpty());
        assertTrue(StreamUtil.toMap(null, s -> s).isEmpty());
    }

    @Test
    /**
     * testMapToList方法。
     */
    public void testMapToList() {
        List<String> list = Arrays.asList("a", "bb", "ccc");
        assertEquals(Arrays.asList(1, 2, 3), StreamUtil.mapToList(list, String::length));
        // 映射结果为null时保留
        List<String> withNull = new ArrayList<>(Arrays.asList("a", null, "b"));
        List<String> mapped = StreamUtil.mapToList(withNull, s -> s);
        assertEquals(2, mapped.size());
        // null/空集合
        assertTrue(StreamUtil.mapToList((List<String>) null, s -> s).isEmpty());
    }

    @Test
    /**
     * testMapToDistinctList方法。
     */
    public void testMapToDistinctList() {
        List<String> list = Arrays.asList("a", "bb", "cc", "ddd", null);
        List<Integer> distinct = StreamUtil.mapToDistinctList(list, String::length);
        assertEquals(Arrays.asList(1, 2, 3), distinct);
    }

    @Test
    /**
     * testMapToJoin方法。
     */
    public void testMapToJoin() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", StreamUtil.mapToJoin(list, s -> s));
        assertEquals("a-b-c", StreamUtil.mapToJoin(list, s -> s, "-"));
        // 空集合返回空字符串
        assertEquals("", StreamUtil.mapToJoin(new ArrayList<String>(), s -> s));
    }

    @Test
    /**
     * testMapToListnonnull方法。
     */
    public void testMapToListnonnull() {
        Map<String, String> dict = new HashMap<>();
        dict.put("a", "1");
        // 未命中键映射为null时剔除
        List<String> keys = new ArrayList<>(Arrays.asList("a", "missing", null));
        List<String> values = StreamUtil.mapToListNonnull(keys, s -> dict.get(s));
        assertEquals(Arrays.asList("1"), values);
    }

    @Test
    /**
     * testMapToListWithFilter方法。
     */
    public void testMapToListWithFilter() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "bb", null, "ccc"));
        List<Integer> lens = StreamUtil.mapToList(list, s -> s.length() > 1, String::length);
        assertEquals(Arrays.asList(2, 3), lens);
    }

    @Test
    /**
     * testMapToSet方法。
     */
    public void testMapToSet() {
        List<String> list = Arrays.asList("a", "bb", "cc", "ccc");
        assertEquals(3, StreamUtil.mapToSet(list, String::length).size());
        assertEquals(1, StreamUtil.mapToSet(list, s -> s.length() > 2, String::length).size());
    }

    @Test
    /**
     * testListToGroup方法。
     */
    public void testListToGroup() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "bb", "cc", null, "d"));
        // 元素自身作为值
        Map<Integer, List<String>> grouped = StreamUtil.listToGroup(list, String::length);
        assertEquals(Arrays.asList("a", "d"), grouped.get(1));
        assertEquals(Arrays.asList("bb", "cc"), grouped.get(2));
        // 值映射
        Map<Integer, List<Integer>> lenOfLen = StreamUtil.listToGroup(list, String::length, String::length);
        assertEquals(Arrays.asList(1, 1), lenOfLen.get(1));
        // key映射为null的元素被剔除
        List<String> nullableKey = new ArrayList<>(Arrays.asList("a", "b"));
        Map<String, List<String>> g = StreamUtil.listToGroup(nullableKey, s -> "a".equals(s) ? "K" : null);
        assertEquals(1, g.size());
        // 空集合
        assertTrue(StreamUtil.listToGroup(new ArrayList<String>(), s -> s).isEmpty());
    }

    @Test
    /**
     * testListToGroupJoin方法。
     */
    public void testListToGroupJoin() {
        List<String> list = Arrays.asList("a", "bb", "cc", "ddd");
        Map<Integer, String> joined = StreamUtil.listToGroupJoin(list, String::length, s -> s);
        assertEquals("bb,cc", joined.get(2));
        // 空集合
        assertTrue(StreamUtil.listToGroupJoin(new ArrayList<String>(), s -> s, s -> s).isEmpty());
    }

    @Test
    /**
     * testRemoveNull方法。
     */
    public void testRemoveNull() {
        List<String> withNull = new ArrayList<>(Arrays.asList("a", null, "b", null));
        List<String> cleaned = StreamUtil.removeNull(withNull);
        assertEquals(Arrays.asList("a", "b"), cleaned);
        // 入参集合不被修改
        assertEquals(4, withNull.size());
        // null/空集合
        assertTrue(StreamUtil.removeNull(null).isEmpty());
    }

    @Test
    /**
     * testSort方法。
     */
    public void testSort() {
        List<Integer> list = new ArrayList<>(Arrays.asList(3, 1, 2));
        StreamUtil.sort(list, Comparator.naturalOrder());
        assertEquals(Arrays.asList(1, 2, 3), list);
        // null/空不抛异常
        StreamUtil.sort((List<Integer>) null, Comparator.naturalOrder());
        StreamUtil.sort(new ArrayList<Integer>(), Comparator.naturalOrder());
    }

    @Test
    /**
     * testSplitToSet方法。
     */
    public void testSplitToSet() {
        assertEquals(3, StreamUtil.splitToSet("a,b,c").size());
        assertTrue(StreamUtil.splitToSet("a,,b,").contains("a"));
        // 正则元字符分隔符按字面量处理
        assertEquals(2, StreamUtil.splitToSet("a|b", "|").size());
        assertEquals(2, StreamUtil.splitToSet("a.b", ".").size());
        // 空白输入
        assertTrue(StreamUtil.splitToSet(null).isEmpty());
        assertTrue(StreamUtil.splitToSet("  ").isEmpty());
    }

    @Test
    /**
     * testSplitToList方法。
     */
    public void testSplitToList() {
        assertEquals(Arrays.asList("a", "b"), StreamUtil.splitToList("a,b", ","));
        // 跳过空白项，其余项保持原样
        assertEquals(Arrays.asList(" a ", " b"), StreamUtil.splitToList(" a , b", ","));
        // 空输入
        assertTrue(StreamUtil.splitToList(null, ",").isEmpty());
        assertTrue(StreamUtil.splitToList("a", null).isEmpty());
        assertTrue(StreamUtil.splitToList("a", "").isEmpty());
    }

    @Test
    /**
     * testFilter方法。
     */
    public void testFilter() {
        List<String> list = new ArrayList<>(Arrays.asList("a", null, "bb", "ccc"));
        assertEquals(Arrays.asList("bb", "ccc"), StreamUtil.filter(list, s -> s.length() > 1));
        // null元素先被剔除，不会被条件命中
        assertEquals(Arrays.asList("a"), StreamUtil.filter(list, s -> s.length() == 1));
        // null/空集合
        assertTrue(StreamUtil.filter(null, s -> true).isEmpty());
        assertTrue(StreamUtil.filter(new ArrayList<String>(), s -> true).isEmpty());
    }

    @Test
    /**
     * testFindAny方法。
     */
    public void testFindAny() {
        List<String> list = new ArrayList<>(Arrays.asList("a", null, "bb"));
        assertEquals("bb", StreamUtil.findAny(list, s -> s.startsWith("b")));
        // 无匹配
        assertNull(StreamUtil.findAny(list, s -> s.startsWith("z")));
        // null/空集合
        assertNull(StreamUtil.findAny(null, s -> true));
        assertNull(StreamUtil.findAny(new ArrayList<String>(), s -> true));
    }

    @Test
    /**
     * testDistinct方法。
     */
    public void testDistinct() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "a", null, "b"));
        assertEquals(Arrays.asList("a", "b"), StreamUtil.distinct(list));
        // 不修改入参
        assertEquals(4, list.size());
        // null/空集合
        assertTrue(StreamUtil.distinct(null).isEmpty());
    }

    @Test
    /**
     * testMapToDistinctListWithFilter方法。
     */
    public void testMapToDistinctListWithFilter() {
        List<String> list = Arrays.asList("a", "bb", "ccc", "dd");
        // 过滤长度大于1后映射长度再去重
        assertEquals(Arrays.asList(2, 3), StreamUtil.mapToDistinctList(list, s -> s.length() > 1, String::length));
        // null/空集合
        assertTrue(StreamUtil.mapToDistinctList(null, s -> true, String::length).isEmpty());
    }

    @Test
    /**
     * testToMapWithFilter方法。
     */
    public void testToMapWithFilter() {
        List<String> list = Arrays.asList("a", "bb", "ccc");
        Map<String, Integer> m = StreamUtil.toMap(list, s -> s.length() > 1, s -> "k_" + s, String::length);
        assertEquals(2, m.size());
        assertEquals(Integer.valueOf(2), m.get("k_bb"));
        // 空集合
        assertTrue(StreamUtil.toMap(new ArrayList<String>(), s -> true, s -> s, s -> 1).isEmpty());
    }

    @Test
    /**
     * testMultiGet方法。
     */
    public void testMultiGet() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", null);
        assertEquals(Arrays.asList(1, 2), StreamUtil.multiGet(map, Arrays.asList("a", "b", "c", "d")));
        // null入参
        assertTrue(StreamUtil.multiGet(null, Arrays.asList("a")).isEmpty());
        assertTrue(StreamUtil.multiGet(map, null).isEmpty());
    }

    @Test
    /**
     * testDistinctByKey方法。
     */
    public void testDistinctByKey() {
        List<String> list = Arrays.asList("a", "bb", "cc", "ddd");
        // 按长度去重，保留首个（同一个Predicate复用，维持内部已见key状态）
        java.util.function.Predicate<String> predicate = StreamUtil.distinctByKey(String::length);
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (predicate.test(s)) {
                result.add(s);
            }
        }
        assertEquals(Arrays.asList("a", "bb", "ddd"), result);
    }

    @Test
    /**
     * testDistinctNotByKey方法。
     */
    public void testDistinctNotByKey() {
        List<String> list = Arrays.asList("a", "bb", "cc", "ddd");
        // 只保留重复项（不含首个）
        java.util.function.Predicate<String> predicate = StreamUtil.distinctNotByKey(String::length);
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (predicate.test(s)) {
                result.add(s);
            }
        }
        assertEquals(Arrays.asList("cc"), result);
    }

    @Test
    /**
     * testConcat方法。
     */
    public void testConcat() {
        List<String> a = Arrays.asList("a", "b");
        List<String> b = Arrays.asList("c", "d");
        assertEquals(Arrays.asList("a", "b", "c", "d"), StreamUtil.concat(a, b));
        // 单侧null
        assertEquals(Arrays.asList("a", "b"), StreamUtil.concat(a, null));
        assertEquals(Arrays.asList("c", "d"), StreamUtil.concat(null, b));
        // 不修改入参
        assertEquals(2, a.size());
        assertTrue(StreamUtil.concat(null, null).isEmpty());
    }

    @Test
    /**
     * testCombine方法。
     */
    public void testCombine() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        // 元素已存在时去重
        assertEquals(Arrays.asList("a", "b"), StreamUtil.combine("a", list));
        // 新元素追加在后
        assertEquals(Arrays.asList("a", "b", "c"), StreamUtil.combine("c", list));
        // null元素忽略
        assertEquals(Arrays.asList("a", "b"), StreamUtil.combine(null, list));
        // 空集合
        assertEquals(Arrays.asList("x"), StreamUtil.combine("x", null));
        assertTrue(StreamUtil.combine(null, null).isEmpty());
        // 不修改入参
        assertEquals(2, list.size());
    }

    @Test
    /**
     * testListToGroupWithNullKey方法。
     */
    public void testListToGroupWithNullKey() {
        List<String> list = Arrays.asList("apple", "banana", "avocado", null);
        Map<Character, List<String>> result = StreamUtil.listToGroupWithNullKey(list, s -> s == null ? null : s.charAt(0));
        // null key归入null键组，null元素跳过
        assertEquals(2, result.size());
        assertEquals(Arrays.asList("apple", "avocado"), result.get('a'));
        assertEquals(Arrays.asList("banana"), result.get('b'));
        // key函数返回null时保留分组
        Map<String, List<String>> nullKeyResult = StreamUtil.listToGroupWithNullKey(
                Arrays.asList("x", "y"), s -> null);
        assertEquals(1, nullKeyResult.size());
        assertEquals(Arrays.asList("x", "y"), nullKeyResult.get(null));
        // 入参null或空时返回空Map
        assertTrue(StreamUtil.listToGroupWithNullKey(null, s -> s).isEmpty());
        assertTrue(StreamUtil.listToGroupWithNullKey(new ArrayList<String>(), s -> s).isEmpty());
    }

    @Test
    /**
     * testGroup方法。
     */
    public void testGroup() {
        List<Integer> list = Arrays.asList(1, 1, 2, 2, 1, 3);
        // 按奇偶等价分组（奇数含3）
        List<List<Integer>> result = StreamUtil.group(list, (a, b) -> Integer.compare(a % 2, b % 2));
        assertEquals(2, result.size());
        assertEquals(Arrays.asList(1, 1, 1, 3), result.get(0));
        assertEquals(Arrays.asList(2, 2), result.get(1));
        // 按值完全等价分组
        List<List<Integer>> byValue = StreamUtil.group(list, Integer::compare);
        assertEquals(3, byValue.size());
        assertEquals(Arrays.asList(1, 1, 1), byValue.get(0));
        assertEquals(Arrays.asList(2, 2), byValue.get(1));
        assertEquals(Arrays.asList(3), byValue.get(2));
        // 入参null或空时返回空列表
        assertTrue(StreamUtil.group(null, Integer::compare).isEmpty());
        assertTrue(StreamUtil.group(new ArrayList<Integer>(), Integer::compare).isEmpty());
    }

    @Test
    /**
     * testMergeDistinct方法。
     */
    public void testMergeDistinct() {
        List<String> a = Arrays.asList("a", "b", null, "c");
        List<String> b = Arrays.asList("b", "d");
        List<String> c = Arrays.asList("e", null);
        // 合并去重，过滤null元素，保持首次出现顺序
        assertEquals(Arrays.asList("a", "b", "c", "d", "e"), StreamUtil.mergeDistinct(a, b, c));
        // 单集合
        assertEquals(Arrays.asList("a", "b", "c"), StreamUtil.mergeDistinct(a));
        // null与空集合跳过
        assertTrue(StreamUtil.mergeDistinct(null, null).isEmpty());
        assertEquals(Arrays.asList("b", "d"), StreamUtil.mergeDistinct(null, b, new ArrayList<String>()));
        // 不修改入参
        assertEquals(4, a.size());
    }

    @Test
    /**
     * testDistinctByKey_NullKey方法。
     */
    public void testDistinctByKey_NullKey() {
        List<String> list = Arrays.asList("a", null, "b", null, "a");
        java.util.function.Predicate<String> predicate = StreamUtil.distinctByKey(s -> s);
        // key为null时不过滤直接保留，不会抛NPE
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (predicate.test(s)) {
                result.add(s);
            }
        }
        assertEquals(Arrays.asList("a", null, "b", null), result);
    }

    @Test
    /**
     * testDistinctNotByKey_NullKey方法。
     */
    public void testDistinctNotByKey_NullKey() {
        List<String> list = Arrays.asList("a", null, "a");
        java.util.function.Predicate<String> predicate = StreamUtil.distinctNotByKey(s -> s);
        // key为null时无重复可言不保留，重复key只保留后续出现的
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (predicate.test(s)) {
                result.add(s);
            }
        }
        assertEquals(Arrays.asList("a"), result);
    }
}
