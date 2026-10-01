package com.zifang.util.proxy.bytecode.model.readtype;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * U1/U2/U4/U8 大端读取语义测试
 * <p>
 * 钉住传输口径：按 JVM 规范大端拼装；存储宽度决定溢出后的符号表现。
 */
public class UTypeReadTest {

    private static InputStream bytes(int... vals) {
        byte[] b = new byte[vals.length];
        for (int i = 0; i < vals.length; i++) {
            b[i] = (byte) vals[i];
        }
        return new ByteArrayInputStream(b);
    }

    @Test
    public void u1ReadsSingleByte() {
        assertEquals(0x00, U1.read(bytes(0x00)).value);
        assertEquals(0x7F, U1.read(bytes(0x7F)).value);
        // U1 存储是 byte，0xFF 回读为 -1
        assertEquals((byte) 0xFF, U1.read(bytes(0xFF)).value);
        assertEquals((byte) 0xFF, U1.read(bytes(0xFF)).getValue());
    }

    @Test
    public void u2AssemblesBigEndian() {
        assertEquals((short) 0x1234, U2.read(bytes(0x12, 0x34)).value);
        assertEquals((short) 0x0034, U2.read(bytes(0x00, 0x34)).getValue());
        // 低位字节为 0，保证符号位来自高位
        assertEquals((short) 0x3400, U2.read(bytes(0x34, 0x00)).value);
    }

    @Test
    public void u2StorageIsShortSoHighBitWrapsNegative() {
        assertEquals((short) 0x8000, U2.read(bytes((byte) 0x80, 0x00)).value);
        assertEquals((short) -1, U2.read(bytes((byte) 0xFF, (byte) 0xFF)).value);
    }

    @Test
    public void u4AssemblesBigEndian() {
        assertEquals(0x00000001, U4.read(bytes(0x00, 0x00, 0x00, 0x01)).value);
        assertEquals(0x12345678, U4.read(bytes(0x12, 0x34, 0x56, 0x78)).value);
    }

    @Test
    public void u4MagicCafebabe() {
        U4 magic = U4.read(bytes((byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE));
        assertEquals(0xCAFEBABE, magic.value);
        assertEquals(0xCAFEBABE, magic.getValue());
        assertNotNull(magic.bytes);
        assertEquals(4, magic.bytes.length);
    }

    @Test
    public void u4AllOnesWrapsNegative() {
        assertEquals(-1, U4.read(bytes((byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF)).value);
    }

    @Test
    public void u8AssemblesBigEndian() {
        assertEquals(0x0123456789ABCDEFL, U8.read(bytes(
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF)).getValue());
        assertEquals(0x0000000000000001L, U8.read(bytes(
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01)).getValue());
    }

    @Test
    public void u8ExtremesWrapByLongStorage() {
        assertEquals(Long.MAX_VALUE, U8.read(bytes(
                0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF)).getValue());
        assertEquals(Long.MIN_VALUE, U8.read(bytes(
                (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)).getValue());
    }
}
