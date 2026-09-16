package com.zifang.util.core.lang.collection;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * CollectionsTest类。
 */
public class CollectionsTest {

    // --- union ---

    @Test
    /**
     * testUnion_NormalCase方法。
     */
    public void testUnion_NormalCase() {
        Collection<String> c1 = Arrays.asList("a", "b", "c");
        Collection<String> c2 = Arrays.asList("b", "c", "d");
        Collection<String> result = Collections.union(c1, c2);
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c", "d")));
        assertEquals(4, result.size());
    }

    @Test
    /**
     * testUnion_SingleCollection方法。
     */
    public void testUnion_SingleCollection() {
        Collection<Integer> c = Arrays.asList(1, 2, 3);
        Collection<Integer> result = Collections.union(c);
        assertEquals(3, result.size());
    }

    @Test
    /**
     * testUnion_WithDuplicatesAcrossCollections方法。
     */
    public void testUnion_WithDuplicatesAcrossCollections() {
        Collection<String> c1 = Arrays.asList("a", "a");
        Collection<String> c2 = Arrays.asList("a", "a");
        Collection<String> result = Collections.union(c1, c2);
        assertEquals(1, result.size());
    }

    @Test
    /**
     * testUnion_EmptyCollections方法。
     */
    public void testUnion_EmptyCollections() {
        Collection<String> empty = new ArrayList<>();
        Collection<String> result = Collections.union(empty, empty);
        assertTrue(result.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    /**
     * testUnion_NullCollectionInArray方法。
     */
    public void testUnion_NullCollectionInArray() {
        Collection<String> c1 = Arrays.asList("a", "b");
        Collection<String> c2 = null;
        Collections.union(c1, c2);
    }

    // --- retain ---

    @Test
    /**
     * testRetain_NormalCase方法。
     */
    public void testRetain_NormalCase() {
        Collection<String> c1 = Arrays.asList("a", "b", "c");
        Collection<String> c2 = Arrays.asList("b", "c", "d");
        Collection<String> result = Collections.retain(c1, c2);
        assertTrue(result.containsAll(Arrays.asList("b", "c")));
        assertEquals(2, result.size());
    }

    @Test
    /**
     * testRetain_NoOverlap方法。
     */
    public void testRetain_NoOverlap() {
        Collection<String> c1 = Arrays.asList("a", "b");
        Collection<String> c2 = Arrays.asList("c", "d");
        Collection<String> result = Collections.retain(c1, c2);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testRetain_IdenticalCollections方法。
     */
    public void testRetain_IdenticalCollections() {
        Collection<String> c1 = Arrays.asList("a", "b");
        Collection<String> c2 = Arrays.asList("a", "b");
        Collection<String> result = Collections.retain(c1, c2);
        assertEquals(2, result.size());
    }

    // --- isEmptyCollection / isNotEmptyCollection ---

    @Test
    /**
     * testIsEmptyCollection_Null方法。
     */
    public void testIsEmptyCollection_Null() {
        assertTrue(Collections.isEmptyCollection(null));
    }

    @Test
    /**
     * testIsEmptyCollection_Empty方法。
     */
    public void testIsEmptyCollection_Empty() {
        assertTrue(Collections.isEmptyCollection(new ArrayList<>()));
    }

    @Test
    /**
     * testIsEmptyCollection_Normal方法。
     */
    public void testIsEmptyCollection_Normal() {
        assertFalse(Collections.isEmptyCollection(Arrays.asList("a")));
    }

    @Test
    /**
     * testIsNotEmptyCollection_Null方法。
     */
    public void testIsNotEmptyCollection_Null() {
        assertFalse(Collections.isNotEmptyCollection(null));
    }

    @Test
    /**
     * testIsNotEmptyCollection_Empty方法。
     */
    public void testIsNotEmptyCollection_Empty() {
        assertFalse(Collections.isNotEmptyCollection(new ArrayList<>()));
    }

    @Test
    /**
     * testIsNotEmptyCollection_Normal方法。
     */
    public void testIsNotEmptyCollection_Normal() {
        assertTrue(Collections.isNotEmptyCollection(Arrays.asList("a")));
    }

    // --- isEmptyMap / isNotEmptyMap ---

    @Test
    /**
     * testIsEmptyMap_Null方法。
     */
    public void testIsEmptyMap_Null() {
        assertTrue(Collections.isEmptyMap(null));
    }

    @Test
    /**
     * testIsEmptyMap_Empty方法。
     */
    public void testIsEmptyMap_Empty() {
        assertTrue(Collections.isEmptyMap(new HashMap<>()));
    }

    @Test
    /**
     * testIsEmptyMap_Normal方法。
     */
    public void testIsEmptyMap_Normal() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        assertFalse(Collections.isEmptyMap(map));
    }

    @Test
    /**
     * testIsNotEmptyMap_Null方法。
     */
    public void testIsNotEmptyMap_Null() {
        assertFalse(Collections.isNotEmptyMap(null));
    }

    @Test
    /**
     * testIsNotEmptyMap_Empty方法。
     */
    public void testIsNotEmptyMap_Empty() {
        assertFalse(Collections.isNotEmptyMap(new HashMap<>()));
    }

    @Test
    /**
     * testIsNotEmptyMap_Normal方法。
     */
    public void testIsNotEmptyMap_Normal() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        assertTrue(Collections.isNotEmptyMap(map));
    }

    // --- removeDuplicate ---

    @Test
    /**
     * testRemoveDuplicate_Normal方法。
     */
    public void testRemoveDuplicate_Normal() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "a", "c", "b"));
        List<String> result = Collections.removeDuplicate(list);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    /**
     * testRemoveDuplicate_Null方法。
     */
    public void testRemoveDuplicate_Null() {
        List<String> result = Collections.removeDuplicate(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testRemoveDuplicate_Empty方法。
     */
    public void testRemoveDuplicate_Empty() {
        List<String> result = Collections.removeDuplicate(new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testRemoveDuplicate_SingleElement方法。
     */
    public void testRemoveDuplicate_SingleElement() {
        List<String> list = new ArrayList<>();
        list.add("a");
        List<String> result = Collections.removeDuplicate(list);
        assertEquals(1, result.size());
    }

    @Test
    /**
     * testRemoveDuplicate_NoDuplicates方法。
     */
    public void testRemoveDuplicate_NoDuplicates() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        List<String> result = Collections.removeDuplicate(list);
        assertEquals(3, result.size());
    }

    // --- intersection (List, List) ---

    @Test
    /**
     * testIntersectionList_Normal方法。
     */
    public void testIntersectionList_Normal() {
        List<String> l1 = Arrays.asList("a", "b", "c");
        List<String> l2 = Arrays.asList("b", "c", "d");
        List<String> result = Collections.intersection(l1, l2);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("b", "c")));
    }

    @Test
    /**
     * testIntersectionList_NoOverlap方法。
     */
    public void testIntersectionList_NoOverlap() {
        List<String> l1 = Arrays.asList("a", "b");
        List<String> l2 = Arrays.asList("c", "d");
        List<String> result = Collections.intersection(l1, l2);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testIntersectionList_NullList方法。
     */
    public void testIntersectionList_NullList() {
        List<String> l1 = null;
        List<String> l2 = Arrays.asList("a");
        List<String> result = Collections.intersection(l1, l2);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testIntersectionList_EmptyList方法。
     */
    public void testIntersectionList_EmptyList() {
        List<String> l1 = new ArrayList<>();
        List<String> l2 = Arrays.asList("a");
        List<String> result = Collections.intersection(l1, l2);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testIntersectionList_SingleElement方法。
     */
    public void testIntersectionList_SingleElement() {
        List<String> l1 = Arrays.asList("a");
        List<String> l2 = Arrays.asList("a");
        List<String> result = Collections.intersection(l1, l2);
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    // --- intersection (Collection, Collection) ---

    @Test
    /**
     * testIntersectionCollection_Normal方法。
     */
    public void testIntersectionCollection_Normal() {
        Collection<String> c1 = Arrays.asList("a", "b", "c");
        Collection<String> c2 = Arrays.asList("b", "c", "d");
        Set<String> result = Collections.intersection(c1, c2);
        assertEquals(2, result.size());
    }

    @Test
    /**
     * testIntersectionCollection_NoOverlap方法。
     */
    public void testIntersectionCollection_NoOverlap() {
        Collection<String> c1 = Arrays.asList("a", "b");
        Collection<String> c2 = Arrays.asList("c", "d");
        Set<String> result = Collections.intersection(c1, c2);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testIntersectionCollection_Null方法。
     */
    public void testIntersectionCollection_Null() {
        Collection<String> c1 = null;
        Collection<String> c2 = Arrays.asList("a");
        Set<String> result = Collections.intersection(c1, c2);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- intersection (Map, Map) ---

    @Test
    /**
     * testIntersectionMap_Normal方法。
     */
    public void testIntersectionMap_Normal() {
        Map<String, Integer> m1 = new HashMap<>();
        m1.put("a", 1);
        m1.put("b", 2);
        m1.put("c", 3);
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("b", 2);
        m2.put("c", 3);
        m2.put("d", 4);
        Map<String, Integer> result = Collections.intersection(m1, m2);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("b"));
        assertTrue(result.containsKey("c"));
    }

    @Test
    /**
     * testIntersectionMap_NoOverlap方法。
     */
    public void testIntersectionMap_NoOverlap() {
        Map<String, Integer> m1 = new HashMap<>();
        m1.put("a", 1);
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("b", 2);
        Map<String, Integer> result = Collections.intersection(m1, m2);
        assertTrue(result.isEmpty());
    }

    // NOTE: intersection(Map, Map) NPEs on null map1 because it calls map1.size() before null-check.
    // This is a source-level bug; test documents actual behavior.
    @Test(expected = NullPointerException.class)
    /**
     * testIntersectionMap_NullFirst方法。
     */
    public void testIntersectionMap_NullFirst() {
        Map<String, Integer> m1 = null;
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("a", 1);
        Collections.intersection(m1, m2);
    }

    @Test
    /**
     * testIntersectionMap_EmptyFirst方法。
     */
    public void testIntersectionMap_EmptyFirst() {
        Map<String, Integer> m1 = new HashMap<>();
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("a", 1);
        Map<String, Integer> result = Collections.intersection(m1, m2);
        assertTrue(result.isEmpty());
    }

    // --- unicon (List, List) ---

    @Test
    /**
     * testUniconList_Normal方法。
     */
    public void testUniconList_Normal() {
        List<String> l1 = Arrays.asList("a", "b");
        List<String> l2 = Arrays.asList("c", "d");
        List<String> result = Collections.unicon(l1, l2);
        assertEquals(4, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c", "d")));
    }

    @Test
    /**
     * testUniconList_WithDuplicates方法。
     */
    public void testUniconList_WithDuplicates() {
        List<String> l1 = Arrays.asList("a", "b");
        List<String> l2 = Arrays.asList("b", "c");
        List<String> result = Collections.unicon(l1, l2);
        assertEquals(4, result.size());
    }

    @Test
    /**
     * testUniconList_EmptySecond方法。
     */
    public void testUniconList_EmptySecond() {
        List<String> l1 = Arrays.asList("a", "b");
        List<String> l2 = new ArrayList<>();
        List<String> result = Collections.unicon(l1, l2);
        assertEquals(2, result.size());
    }

    // --- unicon (Set, Set) ---

    @Test
    /**
     * testUniconSet_Normal方法。
     */
    public void testUniconSet_Normal() {
        Set<String> s1 = new HashSet<>(Arrays.asList("a", "b"));
        Set<String> s2 = new HashSet<>(Arrays.asList("b", "c"));
        Set<String> result = Collections.unicon(s1, s2);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    // --- unicon (Queue, Queue) ---

    @Test
    /**
     * testUniconQueue_Normal方法。
     */
    public void testUniconQueue_Normal() {
        Queue<String> q1 = new LinkedList<>(Arrays.asList("a", "b"));
        Queue<String> q2 = new LinkedList<>(Arrays.asList("c", "d"));
        Queue<String> result = Collections.unicon(q1, q2);
        assertEquals(4, result.size());
    }

    // --- unicon (Map, Map) ---

    @Test
    /**
     * testUniconMap_Normal方法。
     */
    public void testUniconMap_Normal() {
        Map<String, Integer> m1 = new HashMap<>();
        m1.put("a", 1);
        m1.put("b", 2);
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("c", 3);
        m2.put("d", 4);
        Map<String, Integer> result = Collections.unicon(m1, m2);
        assertEquals(4, result.size());
    }

    @Test
    /**
     * testUniconMap_OverlappingKeys方法。
     */
    public void testUniconMap_OverlappingKeys() {
        Map<String, Integer> m1 = new HashMap<>();
        m1.put("a", 1);
        m1.put("b", 2);
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("b", 20);
        m2.put("c", 3);
        Map<String, Integer> result = Collections.unicon(m1, m2);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(20), result.get("b"));
    }

    // --- subtract (List, List) ---

    @Test
    /**
     * testSubtractList_Normal方法。
     */
    public void testSubtractList_Normal() {
        List<String> l1 = Arrays.asList("a", "b", "c");
        List<String> l2 = Arrays.asList("b", "c");
        List<String> result = Collections.subtract(l1, l2);
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    @Test
    /**
     * testSubtractList_NoOverlap方法。
     */
    public void testSubtractList_NoOverlap() {
        List<String> l1 = Arrays.asList("a", "b");
        List<String> l2 = Arrays.asList("c", "d");
        List<String> result = Collections.subtract(l1, l2);
        assertEquals(2, result.size());
    }

    // NOTE: subtract(List, List) NPEs on null list1 because it creates new ArrayList<>(list1.size()) before null-check.
    @Test(expected = NullPointerException.class)
    /**
     * testSubtractList_NullFirst方法。
     */
    public void testSubtractList_NullFirst() {
        List<String> l1 = null;
        List<String> l2 = Arrays.asList("a");
        Collections.subtract(l1, l2);
    }

    @Test
    /**
     * testSubtractList_EmptyFirst方法。
     */
    public void testSubtractList_EmptyFirst() {
        List<String> l1 = new ArrayList<>();
        List<String> l2 = Arrays.asList("a");
        List<String> result = Collections.subtract(l1, l2);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testSubtractList_SingleElement方法。
     */
    public void testSubtractList_SingleElement() {
        List<String> l1 = Arrays.asList("a");
        List<String> l2 = Arrays.asList("a");
        List<String> result = Collections.subtract(l1, l2);
        assertTrue(result.isEmpty());
    }

    // --- subtract (Set, Set) ---

    @Test
    /**
     * testSubtractSet_Normal方法。
     */
    public void testSubtractSet_Normal() {
        Set<String> s1 = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<String> s2 = new HashSet<>(Arrays.asList("b", "c"));
        Set<String> result = Collections.subtract(s1, s2);
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    // NOTE: subtract(Set, Set) NPEs on null set1 because it creates new HashSet<>(set1.size()) before null-check.
    @Test(expected = NullPointerException.class)
    /**
     * testSubtractSet_NullFirst方法。
     */
    public void testSubtractSet_NullFirst() {
        Set<String> s1 = null;
        Set<String> s2 = new HashSet<>(Arrays.asList("a"));
        Collections.subtract(s1, s2);
    }

    // --- subtract (Queue, Queue) ---

    @Test
    /**
     * testSubtractQueue_Normal方法。
     */
    public void testSubtractQueue_Normal() {
        Queue<String> q1 = new LinkedList<>(Arrays.asList("a", "b", "c"));
        Queue<String> q2 = new LinkedList<>(Arrays.asList("b", "c"));
        Queue<String> result = Collections.subtract(q1, q2);
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    @Test
    /**
     * testSubtractQueue_Null方法。
     */
    public void testSubtractQueue_Null() {
        Queue<String> q1 = null;
        Queue<String> q2 = new LinkedList<>(Arrays.asList("a"));
        Queue<String> result = Collections.subtract(q1, q2);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- subtract (Map, Map) ---

    @Test
    /**
     * testSubtractMap_Normal方法。
     */
    public void testSubtractMap_Normal() {
        Map<String, Integer> m1 = new HashMap<>();
        m1.put("a", 1);
        m1.put("b", 2);
        m1.put("c", 3);
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("b", 2);
        m2.put("c", 3);
        Map<String, Integer> result = Collections.subtract(m1, m2);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("a"));
    }

    @Test
    /**
     * testSubtractMap_NoOverlap方法。
     */
    public void testSubtractMap_NoOverlap() {
        Map<String, Integer> m1 = new HashMap<>();
        m1.put("a", 1);
        m1.put("b", 2);
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("c", 3);
        m2.put("d", 4);
        Map<String, Integer> result = Collections.subtract(m1, m2);
        assertEquals(2, result.size());
    }

    // NOTE: subtract(Map, Map) NPEs on null map1 because it creates new HashMap<>(map1.size()) before null-check.
    @Test(expected = NullPointerException.class)
    /**
     * testSubtractMap_NullFirst方法。
     */
    public void testSubtractMap_NullFirst() {
        Map<String, Integer> m1 = null;
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("a", 1);
        Collections.subtract(m1, m2);
    }

    @Test
    /**
     * testSubtractMap_EmptyFirst方法。
     */
    public void testSubtractMap_EmptyFirst() {
        Map<String, Integer> m1 = new HashMap<>();
        Map<String, Integer> m2 = new HashMap<>();
        m2.put("a", 1);
        Map<String, Integer> result = Collections.subtract(m1, m2);
        assertTrue(result.isEmpty());
    }

    // --- join (Collection, separator) ---

    @Test
    /**
     * testJoinCollection_Normal方法。
     */
    public void testJoinCollection_Normal() {
        Collection<String> c = Arrays.asList("a", "b", "c");
        String result = Collections.join(c, ",");
        assertEquals("a,b,c", result);
    }

    @Test
    /**
     * testJoinCollection_SingleElement方法。
     */
    public void testJoinCollection_SingleElement() {
        Collection<String> c = new ArrayList<>();
        c.add("a");
        String result = Collections.join(c, ",");
        assertEquals("a", result);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    /**
     * testJoinCollection_Empty方法。
     */
    public void testJoinCollection_Empty() {
        Collection<String> c = new ArrayList<>();
        Collections.join(c, ",");
    }

    // NOTE: join with multi-char separator is buggy: it always strips exactly 1 character
    // regardless of separator length, so "a||b||" becomes "a||b|" not "a||b".
    @Test
    /**
     * testJoinCollection_MultiCharSeparator方法。
     */
    public void testJoinCollection_MultiCharSeparator() {
        Collection<String> c = Arrays.asList("a", "b");
        String result = Collections.join(c, "||");
        assertEquals("a||b|", result); // actual buggy output
    }

    // --- join (Map, separator, separator1) ---

    @Test
    /**
     * testJoinMap_Normal方法。
     */
    public void testJoinMap_Normal() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("k1", 1);
        map.put("k2", 2);
        String result = Collections.join(map, ",", "=");
        assertEquals("k1=1,k2=2", result);
    }

    @Test
    /**
     * testJoinMap_SingleEntry方法。
     */
    public void testJoinMap_SingleEntry() {
        Map<String, Integer> map = new HashMap<>();
        map.put("k1", 1);
        String result = Collections.join(map, ",", "=");
        assertEquals("k1=1", result);
    }

    @Test
    /**
     * testJoinMap_Null方法。
     */
    public void testJoinMap_Null() {
        String result = Collections.join((Map<String, Integer>) null, ",", "=");
        assertEquals("", result);
    }

    @Test
    /**
     * testJoinMap_Empty方法。
     */
    public void testJoinMap_Empty() {
        Map<String, Integer> map = new HashMap<>();
        String result = Collections.join(map, ",", "=");
        assertEquals("", result);
    }

    // --- keyJoin ---

    @Test
    /**
     * testKeyJoin_Normal方法。
     */
    public void testKeyJoin_Normal() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        String result = Collections.keyJoin(map, ",");
        assertEquals("a,b,c", result);
    }

    @Test
    /**
     * testKeyJoin_SingleEntry方法。
     */
    public void testKeyJoin_SingleEntry() {
        Map<String, Integer> map = new HashMap<>();
        map.put("k", 1);
        String result = Collections.keyJoin(map, ",");
        assertEquals("k", result);
    }

    // NOTE: keyJoin throws StringIndexOutOfBoundsException on empty map (source bug: substring(0, -1))
    @Test(expected = StringIndexOutOfBoundsException.class)
    /**
     * testKeyJoin_Empty方法。
     */
    public void testKeyJoin_Empty() {
        Map<String, Integer> map = new HashMap<>();
        Collections.keyJoin(map, ",");
    }

    // --- valueJoin ---

    @Test
    /**
     * testValueJoin_Normal方法。
     */
    public void testValueJoin_Normal() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("k1", 1);
        map.put("k2", 2);
        map.put("k3", 3);
        String result = Collections.valueJoin(map, ",");
        assertEquals("1,2,3", result);
    }

    @Test
    /**
     * testValueJoin_SingleEntry方法。
     */
    public void testValueJoin_SingleEntry() {
        Map<String, Integer> map = new HashMap<>();
        map.put("k", 1);
        String result = Collections.valueJoin(map, ",");
        assertEquals("1", result);
    }

    // NOTE: valueJoin throws StringIndexOutOfBoundsException on empty map (source bug: substring(0, -1))
    @Test(expected = StringIndexOutOfBoundsException.class)
    /**
     * testValueJoin_Empty方法。
     */
    public void testValueJoin_Empty() {
        Map<String, Integer> map = new HashMap<>();
        Collections.valueJoin(map, ",");
    }
}
