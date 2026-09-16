package io.zifu.z.serialize.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 低层字节写入器，提供 wire format 原语。
 *
 * <p>所有 {@link ZWriter} 内部通过本类写入；其他模块（JSON/MsgPack/压缩）
 * 也共用本类以保证字节布局一致。</p>
 *
 * <p><b>线程安全：</b>非线程安全，每个线程一个实例。</p>
 *
 * @author zifang
 */
public final class ZOutput {

    private final OutputStream out;
    private final byte[] buf = new byte[10];

    public ZOutput(OutputStream out) {
        this.out = out;
    }

    /** 写 1 字节。 */
    public void writeByte(int v) throws IOException {
        out.write(v);
    }

    /** 写固定 4 字节（小端）。 */
    public void writeFixed32(int v) throws IOException {
        out.write(v & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 24) & 0xFF);
    }

    /** 写固定 8 字节（小端）。 */
    public void writeFixed64(long v) throws IOException {
        writeFixed32((int) v);
        writeFixed32((int) (v >>> 32));
    }

    /** 写无符号 varint。 */
    public void writeVarInt(int v) throws IOException {
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v);
    }

    /** 写无符号 varint（long）。 */
    public void writeVarLong(long v) throws IOException {
        while ((v & ~0x7FL) != 0L) {
            out.write(((int) v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write((int) v);
    }

    /** 写有符号 varint（先 ZigZag）。 */
    public void writeSignedVarInt(int v) throws IOException {
        writeVarInt((v << 1) ^ (v >> 31));
    }

    public void writeSignedVarLong(long v) throws IOException {
        writeVarLong((v << 1) ^ (v >> 63));
    }

    /** 写 IEEE-754 float（小端 4 字节）。 */
    public void writeFloat(float v) throws IOException {
        writeFixed32(Float.floatToRawIntBits(v));
    }

    /** 写 IEEE-754 double（小端 8 字节）。 */
    public void writeDouble(double v) throws IOException {
        writeFixed64(Double.doubleToRawLongBits(v));
    }

    /** 写原始字节数组。 */
    public void writeRawBytes(byte[] data) throws IOException {
        out.write(data);
    }

    public void writeRawBytes(byte[] data, int offset, int length) throws IOException {
        out.write(data, offset, length);
    }

    /** 写 length-delimited 字段：varint(length) + bytes。 */
    public void writeLengthDelimited(byte[] data) throws IOException {
        writeVarInt(data.length);
        out.write(data);
    }

    /** 写 tag（field_id << 3 | wire_type）。 */
    public void writeTag(int fieldId, int wireType) throws IOException {
        writeVarInt((fieldId << 3) | wireType);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }
}
