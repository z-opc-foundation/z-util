package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * LongsTest类。
 */
public class LongsTest {

    @Test
    /**
     * of_singleByte方法。
     */
    public void of_singleByte() {
        byte[] bytes = new byte[]{1};
        assertEquals(1L, Longs.of(bytes));
    }

    @Test
    /**
     * of_multipleBytes方法。
     */
    public void of_multipleBytes() {
        byte[] bytes = new byte[]{1, 2, 3, 4};
        long expected = 1 + (2 << 8) + (3 << 16) + (4 << 24);
        assertEquals(expected, Longs.of(bytes));
    }

    @Test
    /**
     * of_allFF方法。
     */
    public void of_allFF() {
        byte[] bytes = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long expected = 0xffffffffL;
        assertEquals(expected, Longs.of(bytes));
    }

    @Test
    /**
     * of_emptyBytes方法。
     */
    public void of_emptyBytes() {
        byte[] bytes = new byte[]{};
        assertEquals(0L, Longs.of(bytes));
    }
}
