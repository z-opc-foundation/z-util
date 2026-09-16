package com.zifang.util.office.word;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tuple键值对泛型类的单元测试
 */

/**
 * TupleTest类。
 */
public class TupleTest {

    @Test
    /**
     * testConstructorAndGetters方法。
     */
    public void testConstructorAndGetters() {
        Tuple<String, Integer> tuple = new Tuple<>("key1", 100);

        assertEquals("key1", tuple.getKey());
        assertEquals(Integer.valueOf(100), tuple.getValue());
    }

    @Test
    /**
     * testSetters方法。
     */
    public void testSetters() {
        Tuple<String, String> tuple = new Tuple<>(null, null);

        tuple.setKey("testKey");
        tuple.setValue("testValue");

        assertEquals("testKey", tuple.getKey());
        assertEquals("testValue", tuple.getValue());
    }

    @Test
    /**
     * testDifferentTypes方法。
     */
    public void testDifferentTypes() {
        Tuple<Integer, Boolean> tuple = new Tuple<>(42, true);

        assertEquals(Integer.valueOf(42), tuple.getKey());
        assertEquals(Boolean.TRUE, tuple.getValue());
    }
}