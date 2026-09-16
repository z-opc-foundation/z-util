package com.zifang.util.core.lang.serialize;

import java.nio.charset.StandardCharsets;

/**
 * 字节数组的只读视图，提供高效读取原语。
 * <p>
 * 与 {@link ByteWriter} 严格对称。
 * <p>
 * 所有读取方法在数据不足时抛出 {@link IndexOutOfBoundsException}。
 *
 * @author zifang
 */
public class ByteReader {

    private final byte[] buffer;
    private int pos;

    public ByteReader(byte[] buffer) {
        this(buffer, 0);
    }

    public ByteReader(byte[] buffer, int offset) {
        this.buffer = buffer;
        this.pos = offset;
    }

    /**
     * 读取 1 字节。
     */
    public byte readByte() {
        ensureRemaining(1);
        return buffer[pos++];
    }

    /**
     * 读取 n 个字节。
     */
    public byte[] readBytes(int n) {
        ensureRemaining(n);
        byte[] out = new byte[n];
        System.arraycopy(buffer, pos, out, 0, n);
        pos += n;
        return out;
    }

    /**
     * 读取全部剩余字节。
     */
    public byte[] readRemaining() {
        byte[] out = new byte[buffer.length - pos];
        System.arraycopy(buffer, pos, out, 0, out.length);
        pos = buffer.length;
        return out;
    }

    /**
     * 读取 boolean（0=false, 非0=true）。
     */
    public boolean readBoolean() {
        return readByte() != 0;
    }

    /**
     * 读取 short（2 字节大端）。
     */
    public short readShort() {
        ensureRemaining(2);
        int hi = buffer[pos++] & 0xFF;
        int lo = buffer[pos++] & 0xFF;
        return (short) ((hi << 8) | lo);
    }

    /**
     * 读取 int（4 字节大端）。
     */
    public int readInt() {
        ensureRemaining(4);
        int v = ((buffer[pos] & 0xFF) << 24)
                | ((buffer[pos + 1] & 0xFF) << 16)
                | ((buffer[pos + 2] & 0xFF) << 8)
                | (buffer[pos + 3] & 0xFF);
        pos += 4;
        return v;
    }

    /**
     * 读取 Varint 编码的有符号 int。
     */
    public int readVarInt() {
        int start = pos;
        int value = Varint.readSignedInt(buffer, pos);
        // 计算 Varint 占用的字节数
        pos = start + Varint.size((value << 1) ^ (value >> 31));
        return value;
    }

    /**
     * 读取 long（8 字节大端）。
     */
    public long readLong() {
        ensureRemaining(8);
        long v = ((long) (buffer[pos] & 0xFF) << 56)
                | ((long) (buffer[pos + 1] & 0xFF) << 48)
                | ((long) (buffer[pos + 2] & 0xFF) << 40)
                | ((long) (buffer[pos + 3] & 0xFF) << 32)
                | ((long) (buffer[pos + 4] & 0xFF) << 24)
                | ((long) (buffer[pos + 5] & 0xFF) << 16)
                | ((long) (buffer[pos + 6] & 0xFF) << 8)
                | ((long) (buffer[pos + 7] & 0xFF));
        pos += 8;
        return v;
    }

    /**
     * 读取 Varint 编码的有符号 long。
     */
    public long readVarLong() {
        int sPos = pos;
        long value = Varint.readSignedLong(buffer, pos);
        pos = sPos + Varint.size((value << 1) ^ (value >> 63));
        return value;
    }

    /**
     * 读取 float。
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * 读取 double。
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * 读取 UTF-8 字符串。
     * 长度前缀为 -1 时返回 null。
     */
    public String readString() {
        int len = readVarInt();
        if (len == -1) {
            return null;
        }
        if (len == 0) {
            return "";
        }
        ensureRemaining(len);
        String s = new String(buffer, pos, len, StandardCharsets.UTF_8);
        pos += len;
        return s;
    }

    /**
     * 读取 boolean 数组（-1 长度返回 null）。
     */
    public boolean[] readBooleanArray() {
        int len = readVarInt();
        if (len == -1) {
            return null;
        }
        boolean[] arr = new boolean[len];
        for (int i = 0; i < len; i++) {
            arr[i] = readBoolean();
        }
        return arr;
    }

    /**
     * 读取 int 数组。
     */
    public int[] readIntArray() {
        int len = readVarInt();
        if (len == -1) {
            return null;
        }
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = readInt();
        }
        return arr;
    }

    /**
     * 读取 long 数组。
     */
    public long[] readLongArray() {
        int len = readVarInt();
        if (len == -1) {
            return null;
        }
        long[] arr = new long[len];
        for (int i = 0; i < len; i++) {
            arr[i] = readLong();
        }
        return arr;
    }

    /**
     * 读取 double 数组。
     */
    public double[] readDoubleArray() {
        int len = readVarInt();
        if (len == -1) {
            return null;
        }
        double[] arr = new double[len];
        for (int i = 0; i < len; i++) {
            arr[i] = readDouble();
        }
        return arr;
    }

    /**
     * 剩余可读字节数。
     */
    public int remaining() {
        return buffer.length - pos;
    }

    /**
     * 当前读取位置。
     */
    public int position() {
        return pos;
    }

    /**
     * 跳到指定位置。
     */
    public void position(int p) {
        if (p < 0 || p > buffer.length) {
            throw new IndexOutOfBoundsException("position out of range: " + p);
        }
        this.pos = p;
        // 注: p 在方法体中保留以表明意图
    }

    private void ensureRemaining(int n) {
        if (pos + n > buffer.length) {
            throw new IndexOutOfBoundsException(
                    "Not enough bytes: need " + n + " at pos " + pos
                            + " but only " + remaining() + " available");
        }
    }
}
