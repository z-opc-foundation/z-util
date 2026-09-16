package io.zifu.z.serialize.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 低层字节读取器，提供 wire format 原语。
 *
 * @author zifang
 */
public final class ZInput {

    private final InputStream in;

    public ZInput(InputStream in) {
        this.in = in;
    }

    /** 读 1 字节。返回 -1 表示 EOF。 */
    public int readByte() throws IOException {
        return in.read();
    }

    /** 读固定 4 字节（小端）。 */
    public int readFixed32() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        if ((b0 | b1 | b2 | b3) < 0) {
            throw new IOException("Unexpected EOF reading fixed32");
        }
        return (b0) | (b1 << 8) | (b2 << 16) | (b3 << 24);
    }

    /** 读固定 8 字节（小端）。 */
    public long readFixed64() throws IOException {
        long lo = readFixed32() & 0xFFFFFFFFL;
        long hi = readFixed32() & 0xFFFFFFFFL;
        return (hi << 32) | lo;
    }

    /** 读无符号 varint。 */
    public int readVarInt() throws IOException {
        int value = 0;
        int shift = 0;
        int b;
        do {
            b = in.read();
            if (b < 0) throw new IOException("Unexpected EOF in varint");
            value |= (b & 0x7F) << shift;
            shift += 7;
            if (shift > 35) throw new IOException("Varint too long (int overflow)");
        } while ((b & 0x80) != 0);
        return value;
    }

    /** 读无符号 varint（long）。 */
    public long readVarLong() throws IOException {
        long value = 0L;
        int shift = 0;
        int b;
        do {
            b = in.read();
            if (b < 0) throw new IOException("Unexpected EOF in varint");
            value |= ((long) (b & 0x7F)) << shift;
            shift += 7;
            if (shift > 70) throw new IOException("Varint too long (long overflow)");
        } while ((b & 0x80) != 0);
        return value;
    }

    public int readSignedVarInt() throws IOException {
        int raw = readVarInt();
        return (raw >>> 1) ^ -(raw & 1);
    }

    public long readSignedVarLong() throws IOException {
        long raw = readVarLong();
        return (raw >>> 1) ^ -(raw & 1);
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readFixed32());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readFixed64());
    }

    /** 读 length-delimited 字段：varint(length) + bytes。 */
    public byte[] readLengthDelimited() throws IOException {
        int len = readVarInt();
        byte[] data = new byte[len];
        readFully(data, 0, len);
        return data;
    }

    /** 读 length-delimited 字段到提供的字节数组（避免分配）。 */
    public int readLengthDelimited(byte[] dst, int dstOffset) throws IOException {
        int len = readVarInt();
        readFully(dst, dstOffset, len);
        return len;
    }

    /** 跳过 length-delimited 字段（仅跳过字节，不分配数组）。 */
    public long skipLengthDelimited() throws IOException {
        long len = readVarInt() & 0xFFFFFFFFL;
        return skip(len);
    }

    /** 读 tag，返回 (fieldId, wireType)。 */
    public int readTag() throws IOException {
        int tag = readVarInt();
        int fieldId = tag >>> 3;
        int wireType = tag & 0x7;
        if (!WireType.isValid(wireType)) {
            throw new IOException("Invalid wire type: " + wireType);
        }
        return (fieldId << 3) | wireType;  // 返回原始 tag（fieldId*8 + wireType）
    }

    public static int fieldId(int tag) { return tag >>> 3; }
    public static int wireTypeOf(int tag) { return tag & 0x7; }

    /** 读完全部 N 字节（阻塞直至填满或 EOF）。 */
    public void readFully(byte[] buf, int offset, int length) throws IOException {
        int total = 0;
        while (total < length) {
            int n = in.read(buf, offset + total, length - total);
            if (n < 0) throw new IOException("Unexpected EOF");
            total += n;
        }
    }

    public long skip(long n) throws IOException {
        long total = 0;
        while (total < n) {
            long skipped = in.skip(n - total);
            if (skipped <= 0) {
                if (in.read() < 0) break;
                total++;
            } else {
                total += skipped;
            }
        }
        return total;
    }

    /** 是否还有数据可读（流未到末尾）。
     * <p>实现：peek 1 字节，看是否返回 -1。</p>
     */
    public boolean hasMore() throws IOException {
        if (in instanceof java.io.ByteArrayInputStream) {
            return ((java.io.ByteArrayInputStream) in).available() > 0;
        }
        // 通用实现：mark/reset 支持，或临时读取
        if (in.markSupported()) {
            in.mark(1);
            int b = in.read();
            in.reset();
            return b >= 0;
        }
        return false;  // 不支持 mark 的流按 EOF 处理
    }

    /** 把当前流剩余读到一个 ByteArrayOutputStream（用于 tag-skipping）。 */
    public byte[] readBytes(int length) throws IOException {
        byte[] buf = new byte[length];
        readFully(buf, 0, length);
        return buf;
    }
}
