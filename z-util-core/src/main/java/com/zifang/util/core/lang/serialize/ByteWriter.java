package com.zifang.util.core.lang.serialize;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 可变长字节缓冲区，提供高效写入原语。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>底层为字节数组，容量不足时按 2 倍扩容</li>
 *   <li>支持常见基本类型的直接写入，避免中间对象</li>
 *   <li>String 写入先写 UTF-8 字节数（Varint），再写原始字节</li>
 *   <li>所有写入方法非线程安全，单线程使用</li>
 * </ul>
 *
 * @author zifang
 */
public class ByteWriter {

    private byte[] buffer;
    private int size;

    public ByteWriter() {
        this(64);
    }

    public ByteWriter(int initialCapacity) {
        if (initialCapacity < 4) {
            initialCapacity = 4;
        }
        this.buffer = new byte[initialCapacity];
    }

    /**
     * 写入一个字节。
     */
    public void writeByte(int b) {
        ensureCapacity(size + 1);
        buffer[size++] = (byte) b;
    }

    /**
     * 写入字节数组（完整内容）。
     */
    public void writeBytes(byte[] data) {
        if (data == null) {
            writeByte(0);
            return;
        }
        ensureCapacity(size + data.length);
        System.arraycopy(data, 0, buffer, size, data.length);
        size += data.length;
    }

    /**
     * 写入字节数组的子区间。
     */
    public void writeBytes(byte[] data, int offset, int length) {
        ensureCapacity(size + length);
        System.arraycopy(data, offset, buffer, size, length);
        size += length;
    }

    /**
     * 写入 boolean（1 字节：0 / 1）。
     */
    public void writeBoolean(boolean v) {
        writeByte(v ? 1 : 0);
    }

    /**
     * 写入 short（2 字节大端）。
     */
    public void writeShort(short v) {
        ensureCapacity(size + 2);
        buffer[size++] = (byte) (v >>> 8);
        buffer[size++] = (byte) v;
    }

    /**
     * 写入 int（4 字节大端）。
     */
    public void writeInt(int v) {
        ensureCapacity(size + 4);
        buffer[size++] = (byte) (v >>> 24);
        buffer[size++] = (byte) (v >>> 16);
        buffer[size++] = (byte) (v >>> 8);
        buffer[size++] = (byte) v;
    }

    /**
     * 写入 Varint 编码的有符号 int。
     */
    public void writeVarInt(int v) {
        int needed = size + Varint.size((v << 1) ^ (v >> 31));
        ensureCapacity(needed);
        Varint.writeSignedInt(buffer, v, size);
        size = needed;
    }

    /**
     * 写入 long（8 字节大端）。
     */
    public void writeLong(long v) {
        ensureCapacity(size + 8);
        buffer[size++] = (byte) (v >>> 56);
        buffer[size++] = (byte) (v >>> 48);
        buffer[size++] = (byte) (v >>> 40);
        buffer[size++] = (byte) (v >>> 32);
        buffer[size++] = (byte) (v >>> 24);
        buffer[size++] = (byte) (v >>> 16);
        buffer[size++] = (byte) (v >>> 8);
        buffer[size++] = (byte) v;
    }

    /**
     * 写入 Varint 编码的有符号 long。
     */
    public void writeVarLong(long v) {
        int needed = size + Varint.size((v << 1) ^ (v >> 63));
        ensureCapacity(needed);
        Varint.writeSignedLong(buffer, v, size);
        size = needed;
    }

    /**
     * 写入 float（4 字节 IEEE-754）。
     */
    public void writeFloat(float v) {
        writeInt(Float.floatToRawIntBits(v));
    }

    /**
     * 写入 double（8 字节 IEEE-754）。
     */
    public void writeDouble(double v) {
        writeLong(Double.doubleToRawLongBits(v));
    }

    /**
     * 写入 UTF-8 编码的字符串：Varint 长度 + 字节内容。
     * null 字符串写入 -1 长度。
     */
    public void writeString(String s) {
        if (s == null) {
            writeVarInt(-1);
            return;
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        writeBytes(bytes);
    }

    /**
     * 写入 boolean 数组。
     */
    public void writeBooleanArray(boolean[] arr) {
        if (arr == null) {
            writeVarInt(-1);
            return;
        }
        writeVarInt(arr.length);
        for (boolean b : arr) {
            writeBoolean(b);
        }
    }

    /**
     * 写入 int 数组。
     */
    public void writeIntArray(int[] arr) {
        if (arr == null) {
            writeVarInt(-1);
            return;
        }
        writeVarInt(arr.length);
        ensureCapacity(size + arr.length * 4);
        for (int v : arr) {
            buffer[size++] = (byte) (v >>> 24);
            buffer[size++] = (byte) (v >>> 16);
            buffer[size++] = (byte) (v >>> 8);
            buffer[size++] = (byte) v;
        }
    }

    /**
     * 写入 long 数组。
     */
    public void writeLongArray(long[] arr) {
        if (arr == null) {
            writeVarInt(-1);
            return;
        }
        writeVarInt(arr.length);
        for (long v : arr) {
            writeLong(v);
        }
    }

    /**
     * 写入 double 数组。
     */
    public void writeDoubleArray(double[] arr) {
        if (arr == null) {
            writeVarInt(-1);
            return;
        }
        writeVarInt(arr.length);
        for (double v : arr) {
            writeDouble(v);
        }
    }

    /**
     * 获取当前已写入的字节数。
     */
    public int size() {
        return size;
    }

    /**
     * 获取内部缓冲区的副本（仅含已写入部分）。
     */
    public byte[] toBytes() {
        return Arrays.copyOf(buffer, size);
    }

    /**
     * 重置写入位置，开始复用缓冲区。
     */
    public void reset() {
        size = 0;
    }

    private void ensureCapacity(int required) {
        if (required > buffer.length) {
            int newCap = buffer.length;
            while (newCap < required) {
                newCap <<= 1;
            }
            buffer = Arrays.copyOf(buffer, newCap);
        }
    }
}
