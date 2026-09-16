package io.zifu.z.serialize.core;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Wire format 低层原语测试。
 */
public class WireFormatTest {

    @Test
    public void testVarIntRoundTrip() throws IOException {
        int[] values = {0, 1, -1, 127, 128, 16383, -128, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int v : values) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZOutput out = new ZOutput(baos);
            out.writeSignedVarInt(v);
            out.close();
            ZInput in = new ZInput(new ByteArrayInputStream(baos.toByteArray()));
            assertEquals("round-trip " + v, v, in.readSignedVarInt());
        }
    }

    @Test
    public void testVarLongRoundTrip() throws IOException {
        long[] values = {0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE, 1L << 40};
        for (long v : values) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZOutput out = new ZOutput(baos);
            out.writeSignedVarLong(v);
            out.close();
            ZInput in = new ZInput(new ByteArrayInputStream(baos.toByteArray()));
            assertEquals("round-trip " + v, v, in.readSignedVarLong());
        }
    }

    @Test
    public void testFixedRoundTrip() throws IOException {
        double[] doubles = {0d, Math.PI, Double.MAX_VALUE, Double.MIN_VALUE, -3.14};
        for (double v : doubles) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZOutput out = new ZOutput(baos);
            out.writeDouble(v);
            out.close();
            ZInput in = new ZInput(new ByteArrayInputStream(baos.toByteArray()));
            assertEquals(v, in.readDouble(), 0d);
        }
    }

    @Test
    public void testLengthDelimitedRoundTrip() throws IOException {
        byte[] data = "Hello, World! 中文测试".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZOutput out = new ZOutput(baos);
        out.writeLengthDelimited(data);
        out.close();
        ZInput in = new ZInput(new ByteArrayInputStream(baos.toByteArray()));
        assertArrayEquals(data, in.readLengthDelimited());
    }

    @Test
    public void testTagEncoding() throws IOException {
        // field_id=10, wire_type=2 (LENGTH_DELIMITED)
        // tag = (10 << 3) | 2 = 82
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZOutput out = new ZOutput(baos);
        out.writeTag(10, WireType.LENGTH_DELIMITED);
        out.close();
        ZInput in = new ZInput(new ByteArrayInputStream(baos.toByteArray()));
        int tag = in.readTag();
        assertEquals(10, ZInput.fieldId(tag));
        assertEquals(WireType.LENGTH_DELIMITED, ZInput.wireTypeOf(tag));
    }

    @Test
    public void testSmallVarIntTakesOneByte() throws IOException {
        // 0 -> 1 byte [0x00], 1 -> 1 byte [0x01]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ZOutput(baos).writeVarInt(0);
        assertEquals(1, baos.size());
        baos = new ByteArrayOutputStream();
        new ZOutput(baos).writeVarInt(127);
        assertEquals(1, baos.size());
        baos = new ByteArrayOutputStream();
        new ZOutput(baos).writeVarInt(128);
        assertEquals(2, baos.size());
    }

    @Test
    public void testHeaderRoundTrip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] body = "hello body".getBytes();
        Header.writeHeader(baos, 12345, (byte) Magic.FLAG_COMPRESSED, body);

        Header.HeaderInfo info = Header.readHeader(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(12345, info.schemaId);
        assertEquals(true, info.isCompressed());
        assertEquals(body.length, info.bodyLen);
    }

    @Test(expected = IOException.class)
    public void testInvalidMagicThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0xFF);
        baos.write(0xFF);
        Header.readHeader(new ByteArrayInputStream(baos.toByteArray()));
    }
}
