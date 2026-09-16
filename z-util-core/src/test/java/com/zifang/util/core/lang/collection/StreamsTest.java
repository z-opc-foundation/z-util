package com.zifang.util.core.lang.collection;

import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * StreamsTest类。
 */
public class StreamsTest {

    // --- streamOf(Iterator) ---

    @Test
    /**
     * testStreamOfIterator_Normal方法。
     */
    public void testStreamOfIterator_Normal() {
        List<String> list = Arrays.asList("a", "b", "c");
        Iterator<String> iterator = list.iterator();
        Stream<String> stream = Streams.streamOf(iterator);
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    /**
     * testStreamOfIterator_SingleElement方法。
     */
    public void testStreamOfIterator_SingleElement() {
        List<String> list = new ArrayList<>();
        list.add("x");
        Iterator<String> iterator = list.iterator();
        Stream<String> stream = Streams.streamOf(iterator);
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(1, result.size());
    }

    @Test
    /**
     * testStreamOfIterator_Empty方法。
     */
    public void testStreamOfIterator_Empty() {
        List<String> list = new ArrayList<>();
        Iterator<String> iterator = list.iterator();
        Stream<String> stream = Streams.streamOf(iterator);
        List<String> result = stream.collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    // --- streamOf(Iterable) ---

    @Test
    /**
     * testStreamOfIterable_Normal方法。
     */
    public void testStreamOfIterable_Normal() {
        List<String> list = Arrays.asList("a", "b", "c");
        Stream<String> stream = Streams.streamOf((Iterable<String>) list);
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(3, result.size());
    }

    @Test
    /**
     * testStreamOfIterable_SingleElement方法。
     */
    public void testStreamOfIterable_SingleElement() {
        List<String> list = new ArrayList<>();
        list.add("x");
        Stream<String> stream = Streams.streamOf((Iterable<String>) list);
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(1, result.size());
    }

    @Test
    /**
     * testStreamOfIterable_Empty方法。
     */
    public void testStreamOfIterable_Empty() {
        List<String> list = new ArrayList<>();
        Stream<String> stream = Streams.streamOf((Iterable<String>) list);
        List<String> result = stream.collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testStreamOfIterable_Set方法。
     */
    public void testStreamOfIterable_Set() {
        Set<String> set = new HashSet<>(Arrays.asList("a", "b"));
        Stream<String> stream = Streams.streamOf((Iterable<String>) set);
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(2, result.size());
    }

    // --- parallelStreamOf(Iterator) ---

    @Test
    /**
     * testParallelStreamOfIterator_Normal方法。
     */
    public void testParallelStreamOfIterator_Normal() {
        List<String> list = Arrays.asList("a", "b", "c");
        Iterator<String> iterator = list.iterator();
        Stream<String> stream = Streams.parallelStreamOf(iterator);
        assertTrue(stream.isParallel());
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(3, result.size());
    }

    @Test
    /**
     * testParallelStreamOfIterator_Empty方法。
     */
    public void testParallelStreamOfIterator_Empty() {
        List<String> list = new ArrayList<>();
        Iterator<String> iterator = list.iterator();
        Stream<String> stream = Streams.parallelStreamOf(iterator);
        assertTrue(stream.isParallel());
        List<String> result = stream.collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }

    // --- parallelStreamOf(Iterable) ---

    @Test
    /**
     * testParallelStreamOfIterable_Normal方法。
     */
    public void testParallelStreamOfIterable_Normal() {
        List<String> list = Arrays.asList("a", "b", "c");
        Stream<String> stream = Streams.parallelStreamOf((Iterable<String>) list);
        assertTrue(stream.isParallel());
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(3, result.size());
    }

    @Test
    /**
     * testParallelStreamOfIterable_SingleElement方法。
     */
    public void testParallelStreamOfIterable_SingleElement() {
        List<String> list = new ArrayList<>();
        list.add("x");
        Stream<String> stream = Streams.parallelStreamOf((Iterable<String>) list);
        assertTrue(stream.isParallel());
        List<String> result = stream.collect(Collectors.toList());
        assertEquals(1, result.size());
    }

    @Test
    /**
     * testParallelStreamOfIterable_Empty方法。
     */
    public void testParallelStreamOfIterable_Empty() {
        List<String> list = new ArrayList<>();
        Stream<String> stream = Streams.parallelStreamOf((Iterable<String>) list);
        assertTrue(stream.isParallel());
        List<String> result = stream.collect(Collectors.toList());
        assertTrue(result.isEmpty());
    }
}
