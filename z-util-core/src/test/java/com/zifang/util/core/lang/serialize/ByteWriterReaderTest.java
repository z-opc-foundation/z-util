package com.zifang.util.core.lang.serialize;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ByteWriter/ByteReader 单元测试。
 */
public class ByteWriterReaderTest {

    @Test
    public void testWriteAndReadByte() {
        ByteWriter w = new ByteWriter();
        w.writeByte(42);
        assertEquals(1, w.size());

        ByteReader r = new ByteReader(w.toBytes());
        assertEquals((byte) 42, r.readByte());
        assertEquals(0, r.remaining());
    }

    @Test
    public void testWriteAndReadBytes() {
        ByteWriter w = new ByteWriter();
        byte[] data = {1, 2, 3, 4, 5};
        w.writeBytes(data);
        ByteReader r = new ByteReader(w.toBytes());
        assertArrayEquals(data, r.readBytes(5));
        assertEquals(0, r.remaining());
    }

    @Test
    public void testWriteAndReadBoolean() {
        ByteWriter w = new ByteWriter();
        w.writeBoolean(true);
        w.writeBoolean(false);
        ByteReader rd = new ByteReader(w.toBytes());
        assertTrue(rd.readBoolean());
        assertFalse(rd.readBoolean());
    }

    @Test
    public void testWriteAndReadInt() {
        ByteWriter w = new ByteWriter();
        w.writeInt(0x12345678);
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals(0x12345678, r.readInt());
    }

    @Test
    public void testWriteAndReadNegativeInt() {
        ByteWriter w = new ByteWriter();
        w.writeInt(-1);
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals(-1, r.readInt());
    }

    @Test
    public void testWriteAndReadVarInt() {
        int[] values = {0, 1, -1, 100, 1000, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int v : values) {
            ByteWriter w = new ByteWriter();
            w.writeVarInt(v);
            ByteReader r = new ByteReader(w.toBytes());
            assertEquals(v, r.readVarInt());
        }
    }

    @Test
    public void testWriteAndReadLong() {
        ByteWriter w = new ByteWriter();
        w.writeLong(Long.MAX_VALUE);
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals(Long.MAX_VALUE, r.readLong());
    }

    @Test
    public void testWriteAndReadVarLong() {
        long[] values = {0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE, 1L << 50};
        for (long v : values) {
            ByteWriter w = new ByteWriter();
            w.writeVarLong(v);
            ByteReader r = new ByteReader(w.toBytes());
            assertEquals(v, r.readVarLong());
        }
    }

    @Test
    public void testWriteAndReadFloat() {
        ByteWriter w = new ByteWriter();
        w.writeFloat(3.14f);
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals(3.14f, r.readFloat(), 0.0001f);
    }

    @Test
    public void testWriteAndReadDouble() {
        ByteWriter w = new ByteWriter();
        w.writeDouble(3.141592653589793);
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals(3.141592653589793, r.readDouble(), 1e-15);
    }

    @Test
    public void testWriteAndReadString() {
        ByteWriter w = new ByteWriter();
        w.writeString("Hello, 世界!");
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals("Hello, 世界!", r.readString());
    }

    @Test
    public void testWriteAndReadNullString() {
        ByteWriter w = new ByteWriter();
        w.writeString(null);
        ByteReader r = new ByteReader(w.toBytes());
        assertNull(r.readString());
    }

    @Test
    public void testWriteAndReadEmptyString() {
        ByteWriter w = new ByteWriter();
        w.writeString("");
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals("", r.readString());
    }

    @Test
    public void testWriteAndReadIntArray() {
        ByteWriter w = new ByteWriter();
        w.writeIntArray(new int[]{1, 2, 3, 4, 5});
        ByteReader r = new ByteReader(w.toBytes());
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, r.readIntArray());
    }

    @Test
    public void testWriteAndReadNullIntArray() {
        ByteWriter w = new ByteWriter();
        w.writeIntArray(null);
        ByteReader r = new ByteReader(w.toBytes());
        assertNull(r.readIntArray());
    }

    @Test
    public void testWriteAndReadEmptyIntArray() {
        ByteWriter w = new ByteWriter();
        w.writeIntArray(new int[0]);
        ByteReader r = new ByteReader(w.toBytes());
        assertArrayEquals(new int[0], r.readIntArray());
    }

    @Test
    public void testWriterAutoExpand() {
        ByteWriter w = new ByteWriter(2);  // very small initial
        for (int i = 0; i < 10000; i++) {
            w.writeInt(i);
        }
        assertEquals(40000, w.size());

        ByteReader r = new ByteReader(w.toBytes());
        for (int i = 0; i < 10000; i++) {
            assertEquals(i, r.readInt());
        }
    }

    @Test
    public void testWriterReset() {
        ByteWriter w = new ByteWriter();
        w.writeInt(100);
        w.writeInt(200);
        assertEquals(8, w.size());
        w.reset();
        assertEquals(0, w.size());
        w.writeInt(42);
        assertEquals(4, w.size());
    }

    @Test
    public void testReaderPosition() {
        ByteWriter w = new ByteWriter();
        w.writeInt(1);
        w.writeInt(2);
        w.writeInt(3);
        byte[] data = w.toBytes();
        ByteReader r = new ByteReader(data);
        assertEquals(1, r.readInt());
        r.position(0);
        assertEquals(1, r.readInt());
        assertEquals(2, r.readInt());
    }

    @Test
    public void testReadBytesExactLength() {
        ByteWriter w = new ByteWriter();
        byte[] data = {10, 20, 30};
        w.writeBytes(data);
        ByteReader r = new ByteReader(w.toBytes());
        byte[] out = r.readBytes(3);
        assertArrayEquals(data, out);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadInsufficientThrows() {
        ByteWriter w = new ByteWriter();
        w.writeByte(1);
        ByteReader r = new ByteReader(w.toBytes());
        r.readInt();  // only 1 byte available
    }

    @Test
    public void testReadRemaining() {
        ByteWriter w = new ByteWriter();
        w.writeInt(1);
        w.writeInt(2);
        ByteReader r = new ByteReader(w.toBytes());
        assertEquals(8, r.remaining());
        r.readInt();
        assertEquals(4, r.remaining());
    }
}
