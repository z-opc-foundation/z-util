package com.zifang.util.core.lang.collection;

import org.junit.Test;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * ListsTest类。
 */
public class ListsTest {

    // --- of(T... t1) ---

    @Test
    /**
     * testOfVarargs_Normal方法。
     */
    public void testOfVarargs_Normal() {
        List<String> result = Lists.of("a", "b", "c");
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    /**
     * testOfVarargs_SingleElement方法。
     */
    public void testOfVarargs_SingleElement() {
        List<String> result = Lists.of("a");
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    @Test
    /**
     * testOfVarargs_Empty方法。
     */
    public void testOfVarargs_Empty() {
        List<String> result = Lists.of();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testOfVarargs_Integer方法。
     */
    public void testOfVarargs_Integer() {
        List<Integer> result = Lists.of(1, 2, 3);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 2, 3)));
    }

    @Test
    /**
     * testOfVarargs_WithNull方法。
     */
    public void testOfVarargs_WithNull() {
        List<String> result = Lists.of("a", null, "c");
        assertEquals(3, result.size());
        assertTrue(result.contains(null));
    }

    // --- of(String, String) ---

    @Test
    /**
     * testOfStringSplitor_Normal方法。
     */
    public void testOfStringSplitor_Normal() {
        List<String> result = Lists.of("a,b,c", ",");
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    /**
     * testOfStringSplitor_MultipleSplits方法。
     */
    public void testOfStringSplitor_MultipleSplits() {
        List<String> result = Lists.of("a||b||c", "\\|\\|");
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    /**
     * testOfStringSplitor_NoMatch方法。
     */
    public void testOfStringSplitor_NoMatch() {
        List<String> result = Lists.of("abc", ",");
        assertEquals(1, result.size());
        assertTrue(result.contains("abc"));
    }

    @Test
    /**
     * testOfStringSplitor_EmptyResult方法。
     */
    public void testOfStringSplitor_EmptyResult() {
        List<String> result = Lists.of("", ",");
        assertEquals(1, result.size());
        assertTrue(result.contains(""));
    }

    @Test
    /**
     * testOfStringSplitor_LeadingTrailingSpaces方法。
     */
    public void testOfStringSplitor_LeadingTrailingSpaces() {
        List<String> result = Lists.of("a, b, c", ",");
        assertEquals(3, result.size());
        assertTrue(result.contains(" b"));
    }

    // --- of(Iterable<E>) ---

    @Test
    /**
     * testOfIterable_Normal方法。
     */
    public void testOfIterable_Normal() {
        List<String> source = Arrays.asList("a", "b", "c");
        List<String> result = Lists.of(source);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    /**
     * testOfIterable_SingleElement方法。
     */
    public void testOfIterable_SingleElement() {
        List<String> source = new ArrayList<>();
        source.add("a");
        List<String> result = Lists.of(source);
        assertEquals(1, result.size());
    }

    @Test
    /**
     * testOfIterable_Empty方法。
     */
    public void testOfIterable_Empty() {
        List<String> source = new ArrayList<>();
        List<String> result = Lists.of(source);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testOfIterable_Set方法。
     */
    public void testOfIterable_Set() {
        Set<String> source = new HashSet<>(Arrays.asList("a", "b"));
        List<String> result = Lists.of(source);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b")));
    }

    @Test(expected = NullPointerException.class)
    /**
     * testOfIterable_Null方法。
     */
    public void testOfIterable_Null() {
        Lists.of((Iterable<String>) null);
    }

    // --- filter ---

    @Test
    /**
     * testFilter_Normal方法。
     */
    public void testFilter_Normal() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
        Predicate<Integer> even = n -> n % 2 == 0;
        List<Integer> result = Lists.filter(list, even);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(2, 4, 6)));
    }

    @Test
    /**
     * testFilter_SingleElement方法。
     */
    public void testFilter_SingleElement() {
        List<Integer> list = new ArrayList<>();
        list.add(2);
        Predicate<Integer> even = n -> n % 2 == 0;
        List<Integer> result = Lists.filter(list, even);
        assertEquals(1, result.size());
    }

    @Test
    /**
     * testFilter_NoMatch方法。
     */
    public void testFilter_NoMatch() {
        List<Integer> list = Arrays.asList(1, 3, 5);
        Predicate<Integer> even = n -> n % 2 == 0;
        List<Integer> result = Lists.filter(list, even);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testFilter_EmptyList方法。
     */
    public void testFilter_EmptyList() {
        List<Integer> list = new ArrayList<>();
        Predicate<Integer> even = n -> n % 2 == 0;
        List<Integer> result = Lists.filter(list, even);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testFilter_AllMatch方法。
     */
    public void testFilter_AllMatch() {
        List<Integer> list = Arrays.asList(2, 4, 6);
        Predicate<Integer> even = n -> n % 2 == 0;
        List<Integer> result = Lists.filter(list, even);
        assertEquals(3, result.size());
    }

    @Test
    /**
     * testFilter_StringLength方法。
     */
    public void testFilter_StringLength() {
        List<String> list = Arrays.asList("a", "bb", "ccc");
        Predicate<String> longerThanOne = s -> s.length() > 1;
        List<String> result = Lists.filter(list, longerThanOne);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("bb", "ccc")));
    }
}
