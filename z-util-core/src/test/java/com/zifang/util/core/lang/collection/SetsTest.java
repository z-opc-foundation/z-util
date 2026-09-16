package com.zifang.util.core.lang.collection;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * SetsTest类。
 */
public class SetsTest {

    // --- newHashSet (varargs) ---

    @Test
    /**
     * testNewHashSet_Normal方法。
     */
    public void testNewHashSet_Normal() {
        HashSet<String> result = Sets.newHashSet("a", "b", "c");
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    /**
     * testNewHashSet_SingleElement方法。
     */
    public void testNewHashSet_SingleElement() {
        HashSet<String> result = Sets.newHashSet("a");
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    @Test
    /**
     * testNewHashSet_Empty方法。
     */
    public void testNewHashSet_Empty() {
        HashSet<String> result = Sets.newHashSet();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testNewHashSet_WithNull方法。
     */
    public void testNewHashSet_WithNull() {
        HashSet<String> result = Sets.newHashSet("a", null, "c");
        assertEquals(3, result.size());
        assertTrue(result.contains(null));
    }

    @Test
    /**
     * testNewHashSet_WithDuplicates方法。
     */
    public void testNewHashSet_WithDuplicates() {
        HashSet<String> result = Sets.newHashSet("a", "a", "b");
        assertEquals(2, result.size());
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    // --- newHashSet (boolean isSorted, varargs) ---

    @Test
    /**
     * testNewHashSetSorted_False方法。
     */
    public void testNewHashSetSorted_False() {
        // Cast false to avoid ambiguity between newHashSet(T...) and newHashSet(boolean, T...)
        HashSet<String> result = Sets.<String>newHashSet(false, "c", "a", "b");
        assertEquals(3, result.size());
        assertFalse(result instanceof LinkedHashSet);
    }

    @Test
    /**
     * testNewHashSetSorted_True方法。
     */
    public void testNewHashSetSorted_True() {
        HashSet<String> result = Sets.<String>newHashSet(true, "c", "a", "b");
        assertEquals(3, result.size());
        assertTrue("Should be LinkedHashSet when sorted", result instanceof LinkedHashSet);
    }

    @Test
    /**
     * testNewHashSetSorted_NullArray方法。
     */
    public void testNewHashSetSorted_NullArray() {
        HashSet<String> result = Sets.<String>newHashSet(false, (String[]) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testNewHashSetSorted_TrueNullArray方法。
     */
    public void testNewHashSetSorted_TrueNullArray() {
        HashSet<String> result = Sets.<String>newHashSet(true, (String[]) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testNewHashSetSorted_EmptyArray方法。
     */
    public void testNewHashSetSorted_EmptyArray() {
        HashSet<String> result = Sets.<String>newHashSet(false);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testNewHashSetSorted_IntegerElements方法。
     */
    public void testNewHashSetSorted_IntegerElements() {
        HashSet<Integer> result = Sets.<Integer>newHashSet(false, 1, 2, 3);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 2, 3)));
    }

    // --- empty stubs: intersection, difference, union ---

    @Test
    /**
     * testIntersection_Basic方法。
     */
    public void testIntersection_Basic() {
        Set<String> s1 = Sets.newHashSet("a", "b", "c");
        Set<String> s2 = Sets.newHashSet("b", "c", "d");
        Set<String> result = Sets.intersection(s1, s2);
        assertEquals(2, result.size());
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
    }

    @Test
    /**
     * testDifference_Basic方法。
     */
    public void testDifference_Basic() {
        Set<String> s1 = Sets.newHashSet("a", "b", "c");
        Set<String> s2 = Sets.newHashSet("b", "c", "d");
        Set<String> result = Sets.difference(s1, s2);
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    @Test
    /**
     * testUnion_Basic方法。
     */
    public void testUnion_Basic() {
        Set<String> s1 = Sets.newHashSet("a", "b");
        Set<String> s2 = Sets.newHashSet("b", "c");
        Set<String> result = Sets.union(s1, s2);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }
}
