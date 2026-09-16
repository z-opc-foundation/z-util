package com.zifang.util.core.lang.serialize;

/**
 * 变长整数（Varint）编/解码工具。
 * <p>
 * 用于无符号整数的紧凑编码：每个字节使用 7 位数据 + 1 位高位作为"是否还有后续字节"标记。
 * <p>
 * 对于有符号整数：先做 ZigZag 转换 (n &rarr; (n &lt;&lt; 1) ^ (n &gt;&gt;&gt; 31))，
 * 让绝对值小的整数（不论正负）占用更少字节。
 * <p>
 * 字节布局示例：
 * <pre>
 * 数值 0          -> 1 字节: 0x00
 * 数值 1          -> 1 字节: 0x01
 * 数值 -1         -> 1 字节: 0x01 (ZigZag 后)
 * 数值 127        -> 1 字节: 0x7F
 * 数值 128        -> 2 字节: 0x80 0x01
 * 数值 16383      -> 2 字节: 0xFF 0x7F
 * </pre>
 *
 * @author zifang
 */
public final class Varint {

    private Varint() {
    }

    /**
     * 计算编码给定值所需的字节数。
     * 用于预分配缓冲区，避免扩容开销。
     *
     * @param value 无符号或 ZigZag 编码后的整数
     */
    public static int size(int value) {
        int size = 1;
        while ((value & ~0x7F) != 0) {
            value >>>= 7;
            size++;
        }
        return size;
    }

    /**
     * 计算编码给定 long 值所需的字节数。
     */
    public static int size(long value) {
        int size = 1;
        while ((value & ~0x7FL) != 0L) {
            value >>>= 7;
            size++;
        }
        return size;
    }

    /**
     * 将 ZigZag + Varint 编码写入字节数组，从 {@code offset} 处开始写入。
     *
     * @param out    目标字节数组
     * @param value  要编码的有符号 int
     * @param offset 写入起始偏移
     * @return 写入后的偏移（不含）
     */
    public static int writeSignedInt(byte[] out, int value, int offset) {
        int zigzag = (value << 1) ^ (value >> 31);
        return writeUnsigned(out, zigzag, offset);
    }

    /**
     * 将 Varint 编码的无符号 int 写入。
     */
    public static int writeUnsigned(byte[] out, int value, int offset) {
        while ((value & ~0x7F) != 0) {
            out[offset++] = (byte) ((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out[offset++] = (byte) value;
        return offset;
    }

    /**
     * 将 ZigZag + Varint 编码写入字节数组。
     */
    public static int writeSignedLong(byte[] out, long value, int offset) {
        long zigzag = (value << 1) ^ (value >> 63);
        return writeUnsignedLong(out, zigzag, offset);
    }

    /**
     * 将 Varint 编码的无符号 long 写入。
     */
    public static int writeUnsignedLong(byte[] out, long value, int offset) {
        while ((value & ~0x7FL) != 0L) {
            out[offset++] = (byte) (((int) value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out[offset++] = (byte) ((int) value);
        return offset;
    }

    /**
     * 从字节数组解码 ZigZag + Varint 的有符号 int。
     */
    public static int readSignedInt(byte[] in, int offset) {
        int raw = readUnsigned(in, offset);
        return (raw >>> 1) ^ -(raw & 1);
    }

    /**
     * 从字节数组解码 Varint 的无符号 int。
     */
    public static int readUnsigned(byte[] in, int offset) {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            b = in[offset++];
            value |= (b & 0x7F) << shift;
            shift += 7;
            if (shift > 35) {
                throw new IllegalStateException("Varint too long: shift=" + shift);
            }
        } while ((b & 0x80) != 0);
        return value;
    }

    /**
     * 从字节数组解码 ZigZag + Varint 的有符号 long。
     */
    public static long readSignedLong(byte[] in, int offset) {
        long raw = readUnsignedLong(in, offset);
        return (raw >>> 1) ^ -(raw & 1);
    }

    /**
     * 从字节数组解码 Varint 的无符号 long。
     */
    public static long readUnsignedLong(byte[] in, int offset) {
        long value = 0L;
        int shift = 0;
        byte b;
        do {
            b = in[offset++];
            value |= (long) (b & 0x7F) << shift;
            shift += 7;
            if (shift > 70) {
                throw new IllegalStateException("Varint too long: shift=" + shift);
            }
        } while ((b & 0x80) != 0);
        return value;
    }
}
