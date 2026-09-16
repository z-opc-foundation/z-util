package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CollectionOpsTest {

    @Test
    public void testPartition() {
        List<List<Integer>> parts = CollectionOps.partition(Arrays.asList(1, 2, 3, 4, 5), 2);
        assertEquals(3, parts.size());
        assertEquals(Arrays.asList(1, 2), parts.get(0));
        assertEquals(Arrays.asList(3, 4), parts.get(1));
        assertEquals(Arrays.asList(5), parts.get(2));
    }

    @Test
    public void testPartitionExact() {
        List<List<Integer>> parts = CollectionOps.partition(Arrays.asList(1, 2, 3, 4), 2);
        assertEquals(2, parts.size());
        assertEquals(Arrays.asList(1, 2), parts.get(0));
        assertEquals(Arrays.asList(3, 4), parts.get(1));
    }

    @Test
    public void testPartitionEmpty() {
        List<List<Integer>> parts = CollectionOps.partition(Collections.emptyList(), 5);
        assertTrue(parts.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPartitionInvalidSize() {
        CollectionOps.partition(Arrays.asList(1, 2), 0);
    }

    @Test
    public void testFindFirst() {
        Integer found = CollectionOps.findFirst(Arrays.asList(1, 2, 3, 4), i -> i > 2);
        assertEquals(Integer.valueOf(3), found);
    }

    @Test
    public void testFindFirstNone() {
        assertNull(CollectionOps.findFirst(Arrays.asList(1, 2), i -> i > 10));
    }

    @Test
    public void testCount() {
        long c = CollectionOps.count(Arrays.asList(1, 2, 3, 4), i -> i % 2 == 0);
        assertEquals(2, c);
    }

    @Test
    public void testHasIntersection() {
        assertTrue(CollectionOps.hasIntersection(Arrays.asList(1, 2), Arrays.asList(2, 3)));
        assertFalse(CollectionOps.hasIntersection(Arrays.asList(1, 2), Arrays.asList(3, 4)));
        assertFalse(CollectionOps.hasIntersection(Arrays.asList(1, 2), null));
    }

    @Test
    public void testFirstNonNull() {
        assertEquals("b", CollectionOps.firstNonNull(null, null, "b", "c"));
        assertNull(CollectionOps.firstNonNull((String) null));
    }

    @Test
    public void testNoNulls() {
        assertTrue(CollectionOps.noNulls(Arrays.asList(1, 2, 3)));
        assertFalse(CollectionOps.noNulls(Arrays.asList(1, null, 3)));
        assertTrue(CollectionOps.noNulls(Collections.emptyList()));
        assertTrue(CollectionOps.noNulls(null));
    }

    @Test
    public void testSameElements() {
        assertTrue(CollectionOps.sameElements(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)));
        assertFalse(CollectionOps.sameElements(Arrays.asList(1, 2), Arrays.asList(1, 3)));
    }

    @Test
    public void testSafeGet() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a", CollectionOps.get(list, 0));
        assertEquals("c", CollectionOps.get(list, 2));
        assertNull(CollectionOps.get(list, 10));
        assertNull(CollectionOps.get(list, -1));
        assertNull(CollectionOps.get(null, 0));
    }

    @Test
    public void testDebugSizeNoException() {
        CollectionOps.debugSize("test", Arrays.asList(1, 2));
        CollectionOps.debugSize("null", null);
    }
}