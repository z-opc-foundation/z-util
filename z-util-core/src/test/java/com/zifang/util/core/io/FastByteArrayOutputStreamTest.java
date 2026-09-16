package com.zifang.util.core.io;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * FastByteArrayOutputStreamTest类。
 */
public class FastByteArrayOutputStreamTest {

    @Test
    /**
     * testWriteSingleByte方法。
     */
    public void testWriteSingleByte() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write('a');
        out.write('b');
        out.write('c');
        assertEquals(3, out.size());
        assertEquals("abc", out.toString());
    }

    @Test
    /**
     * testWriteByteArray方法。
     */
    public void testWriteByteArray() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        byte[] data = "hello".getBytes();
        out.write(data, 0, data.length);
        assertEquals(5, out.size());
        assertEquals("hello", out.toString());
    }

    @Test
    /**
     * testWritePartialByteArray方法。
     */
    public void testWritePartialByteArray() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        byte[] data = "hello world".getBytes();
        out.write(data, 0, 5);
        assertEquals(5, out.size());
        assertEquals("hello", out.toString());
    }

    @Test
    /**
     * testWriteToOutputStream方法。
     */
    public void testWriteToOutputStream() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write("test".getBytes());
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        out.writeTo(dest);
        assertEquals("test", dest.toString());
    }

    @Test
    /**
     * testToByteArray方法。
     */
    public void testToByteArray() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        byte[] data = "byteArray".getBytes();
        out.write(data, 0, data.length);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    /**
     * testToStringWithCharset方法。
     */
    public void testToStringWithCharset() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        byte[] data = "中文测试".getBytes("GBK");
        out.write(data, 0, data.length);
        assertEquals("中文测试", out.toString("GBK"));
    }

    @Test
    /**
     * testToStringWithCharsetObject方法。
     */
    public void testToStringWithCharsetObject() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write("hello".getBytes());
        assertEquals("hello", out.toString(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test
    /**
     * testReset方法。
     */
    public void testReset() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write("hello".getBytes());
        assertEquals(5, out.size());
        out.reset();
        assertEquals(0, out.size());
        assertEquals("", out.toString());
    }

    @Test
    /**
     * testCloseIsNoOp方法。
     */
    public void testCloseIsNoOp() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write("before close".getBytes());
        out.close(); // no-op
        assertEquals("before close", out.toString());
        out.write(" after close".getBytes()); // still works
        assertEquals("before close after close", out.toString());
    }

    @Test
    /**
     * testEmptyStream方法。
     */
    public void testEmptyStream() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        assertEquals(0, out.size());
        assertEquals("", out.toString());
        assertArrayEquals(new byte[0], out.toByteArray());
    }

    @Test
    /**
     * testLargeWrite方法。
     */
    public void testLargeWrite() {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        byte[] large = new byte[100_000];
        for (int i = 0; i < large.length; i++) {
            large[i] = (byte) (i % 256);
        }
        out.write(large, 0, large.length);
        assertEquals(large.length, out.size());
        assertArrayEquals(large, out.toByteArray());
    }

    @Test
    /**
     * testWriteToMultipleTimes方法。
     */
    public void testWriteToMultipleTimes() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream();
        out.write("first".getBytes());
        ByteArrayOutputStream dest1 = new ByteArrayOutputStream();
        out.writeTo(dest1);
        assertEquals("first", dest1.toString());
        // out still holds data
        assertEquals("first", out.toString());
    }

    @Test
    /**
     * testConstructorWithInitialSize方法。
     */
    public void testConstructorWithInitialSize() throws Exception {
        FastByteArrayOutputStream out = new FastByteArrayOutputStream(2048);
        byte[] data = "init".getBytes();
        out.write(data);
        assertEquals(4, out.size());
    }
}
