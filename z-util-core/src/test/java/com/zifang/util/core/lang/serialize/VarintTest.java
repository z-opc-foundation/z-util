package com.zifang.util.core.lang.serialize;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Varint 单元测试。
 */
public class VarintTest {

    @Test
    public void testSizeZero() {
        assertEquals(1, Varint.size(0));
        assertEquals(1, Varint.size(0L));
    }

    @Test
    public void testSizeSmallPositive() {
        assertEquals(1, Varint.size(1));
        assertEquals(1, Varint.size(127));
    }

    @Test
    public void testSizeBoundary() {
        assertEquals(2, Varint.size(128));
        assertEquals(2, Varint.size(16383));
        assertEquals(3, Varint.size(16384));
    }

    @Test
    public void testSizeMaxInt() {
        assertEquals(5, Varint.size(Integer.MAX_VALUE));
    }

    @Test
    public void testSizeMaxLong() {
        assertEquals(9, Varint.size(Long.MAX_VALUE));
    }

    @Test
    public void testRoundTripSignedInt() {
        int[] values = {0, 1, -1, 127, 128, -128, 16384, -16384,
                Integer.MIN_VALUE, Integer.MAX_VALUE, 42, -42};
        for (int v : values) {
            byte[] buf = new byte[Varint.size((v << 1) ^ (v >> 31))];
            int written = Varint.writeSignedInt(buf, v, 0);
            assertEquals(Varint.size((v << 1) ^ (v >> 31)), written);
            int read = Varint.readSignedInt(buf, 0);
            assertEquals(v, read);
        }
    }

    @Test
    public void testRoundTripSignedLong() {
        long[] values = {0L, 1L, -1L, 127L, 128L, -128L, 16384L,
                Long.MIN_VALUE, Long.MAX_VALUE, 1L << 40, -(1L << 40)};
        for (long v : values) {
            byte[] buf = new byte[Varint.size((v << 1) ^ (v >> 63))];
            int written = Varint.writeSignedLong(buf, v, 0);
            assertEquals(Varint.size((v << 1) ^ (v >> 63)), written);
            long read = Varint.readSignedLong(buf, 0);
            assertEquals(v, read);
        }
    }

    @Test
    public void testWriteWithOffset() {
        byte[] buf = new byte[10];
        int written = Varint.writeSignedInt(buf, 1000, 5);
        assertEquals(7, written);  // 5 + 2 bytes
        int read = Varint.readSignedInt(buf, 5);
        assertEquals(1000, read);
    }

    @Test(expected = IllegalStateException.class)
    public void testOverlongVarintThrows() {
        // 构造一个 > 5 字节的 varint
        byte[] buf = new byte[10];
        for (int i = 0; i < 6; i++) {
            buf[i] = (byte) 0xFF;  // 所有字节都是 continuation
        }
        Varint.readSignedInt(buf, 0);
    }
}
