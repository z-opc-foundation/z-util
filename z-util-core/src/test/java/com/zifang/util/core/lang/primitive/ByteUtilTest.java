package com.zifang.util.core.lang.primitive;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ByteUtilTest类。
 */
public class ByteUtilTest {

    @Test(expected = RuntimeException.class)
    /**
     * merge_withNullOrigin方法。
     */
    public void merge_withNullOrigin() {
        ByteUtil.merge(null);
    }

    @Test
    /**
     * merge_singleArray方法。
     */
    public void merge_singleArray() {
        byte[] result = ByteUtil.merge(new byte[]{1, 2, 3});
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    /**
     * merge_multipleArrays方法。
     */
    public void merge_multipleArrays() {
        byte[] result = ByteUtil.merge(
                new byte[]{1, 2},
                new byte[]{3, 4},
                new byte[]{5}
        );
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, result);
    }

    @Test
    /**
     * merge_withNullExtra方法。
     */
    public void merge_withNullExtra() {
        byte[] result = ByteUtil.merge(new byte[]{1, 2}, null, new byte[]{3});
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    /**
     * merge_withEmptyExtra方法。
     */
    public void merge_withEmptyExtra() {
        byte[] result = ByteUtil.merge(new byte[]{1, 2});
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test(expected = RuntimeException.class)
    /**
     * rightPaddingZero_zeroLength方法。
     */
    public void rightPaddingZero_zeroLength() {
        ByteUtil.rightPaddingZero(new byte[]{1, 2}, 0);
    }

    @Test
    /**
     * rightPaddingZero_noPaddingNeeded方法。
     */
    public void rightPaddingZero_noPaddingNeeded() {
        byte[] result = ByteUtil.rightPaddingZero(new byte[]{1, 2}, 2);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test
    /**
     * rightPaddingZero_paddingNeeded方法。
     */
    public void rightPaddingZero_paddingNeeded() {
        byte[] result = ByteUtil.rightPaddingZero(new byte[]{1, 2, 3}, 2);
        // 3 bytes, 2-byte alignment -> needs 1 padding to make 4 bytes
        assertArrayEquals(new byte[]{1, 2, 3, 0}, result);
    }

    @Test
    /**
     * rightPaddingZero_alreadyAligned方法。
     */
    public void rightPaddingZero_alreadyAligned() {
        byte[] result = ByteUtil.rightPaddingZero(new byte[]{1, 2, 3, 4}, 2);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, result);
    }

    @Test
    /**
     * bytesToHexString_normal方法。
     */
    public void bytesToHexString_normal() {
        assertEquals("0102ff", ByteUtil.bytesToHexString(new byte[]{1, 2, (byte) 0xff}));
    }

    @Test
    /**
     * bytesToHexString_null方法。
     */
    public void bytesToHexString_null() {
        assertNull(ByteUtil.bytesToHexString(null));
    }

    @Test
    /**
     * bytesToHexString_empty方法。
     */
    public void bytesToHexString_empty() {
        assertNull(ByteUtil.bytesToHexString(new byte[]{}));
    }

    @Test
    /**
     * hexStringToBytes_normal方法。
     */
    public void hexStringToBytes_normal() {
        assertArrayEquals(new byte[]{1, 2, (byte) 0xff}, ByteUtil.hexStringToBytes("0102ff"));
    }

    @Test
    /**
     * hexStringToBytes_lowercase方法。
     */
    public void hexStringToBytes_lowercase() {
        assertArrayEquals(new byte[]{1, 2, (byte) 0xff}, ByteUtil.hexStringToBytes("0102FF"));
    }

    @Test
    /**
     * hexStringToBytes_null方法。
     */
    public void hexStringToBytes_null() {
        assertNull(ByteUtil.hexStringToBytes(null));
    }

    @Test
    /**
     * hexStringToBytes_empty方法。
     */
    public void hexStringToBytes_empty() {
        assertNull(ByteUtil.hexStringToBytes(""));
    }

    @Test
    /**
     * hexStringToBytes_roundTrip方法。
     */
    public void hexStringToBytes_roundTrip() {
        byte[] original = new byte[]{0, 1, 127, (byte) 128, (byte) 255};
        String hex = ByteUtil.bytesToHexString(original);
        assertArrayEquals(original, ByteUtil.hexStringToBytes(hex));
    }

    @Test
    /**
     * parseByte_withNull方法。
     */
    public void parseByte_withNull() {
        assertNull(ByteUtil.parseByte(null));
    }

    @Test
    /**
     * parseByte_withValidString方法。
     */
    public void parseByte_withValidString() {
        assertEquals(Byte.valueOf("1"), ByteUtil.parseByte("1"));
        assertEquals(Byte.valueOf("-1"), ByteUtil.parseByte("-1"));
        assertEquals(Byte.valueOf("127"), ByteUtil.parseByte("127"));
    }

    @Test(expected = NumberFormatException.class)
    /**
     * parseByte_withInvalidString方法。
     */
    public void parseByte_withInvalidString() {
        ByteUtil.parseByte("not a number");
    }

    @Test
    /**
     * parseByte_withObject方法。
     */
    public void parseByte_withObject() {
        assertEquals(Byte.valueOf("5"), ByteUtil.parseByte(Integer.valueOf(5)));
        assertEquals(Byte.valueOf("-5"), ByteUtil.parseByte(Integer.valueOf(-5)));
    }

    @Test
    /**
     * parseByteOrDefault_withNull方法。
     */
    public void parseByteOrDefault_withNull() {
        assertEquals(Byte.valueOf("99"), ByteUtil.parseByteOrDefault(null, Byte.valueOf("99")));
        assertNull(ByteUtil.parseByteOrDefault(null, null));
    }

    @Test
    /**
     * parseByteOrDefault_withValidValue方法。
     */
    public void parseByteOrDefault_withValidValue() {
        assertEquals(Byte.valueOf("5"), ByteUtil.parseByteOrDefault("5", Byte.valueOf("99")));
        assertEquals(Byte.valueOf("-5"), ByteUtil.parseByteOrDefault("-5", Byte.valueOf("99")));
    }

    @Test(expected = NumberFormatException.class)
    /**
     * parseByteOrDefault_withInvalidValue方法。
     */
    public void parseByteOrDefault_withInvalidValue() {
        ByteUtil.parseByteOrDefault("notanumber", Byte.valueOf("0"));
    }
}
