package io.zifu.z.serialize.core;

/**
 * 无符号整数工具（Java 没有原生无符号类型，需要手工处理）。
 *
 * <p>所有 wire format 上的整数字段都使用 <b>小端 + varint 或 fixed</b> 编码。
 * 编码前需用本类做转换。</p>
 */
public final class Unsigned {
    private Unsigned() {}

    // ==================== Varint ====================

    /** 计算无符号 varint 编码所需字节数。 */
    public static int varIntSize(int value) {
        int size = 1;
        while ((value & ~0x7F) != 0) {
            value >>>= 7;
            size++;
        }
        return size;
    }

    /** 计算无符号 varint 编码所需字节数。 */
    public static int varLongSize(long value) {
        int size = 1;
        while ((value & ~0x7FL) != 0L) {
            value >>>= 7;
            size++;
        }
        return size;
    }

    /** 计算有符号 varint 编码所需字节数（先 ZigZag）。 */
    public static int signedVarIntSize(int value) {
        return varIntSize((value << 1) ^ (value >> 31));
    }

    public static int signedVarLongSize(long value) {
        return varLongSize((value << 1) ^ (value >> 63));
    }

    // ==================== Fixed ====================

    public static final int FIXED32_SIZE = 4;
    public static final int FIXED64_SIZE = 8;

    // ==================== Limits ====================

    /** uint32 上限（Java 中用 long 表示）。 */
    public static final long UINT32_MAX = 0xFFFFFFFFL;
    /** int32 上限（同 Integer.MAX_VALUE）。 */
    public static final int INT32_MAX = Integer.MAX_VALUE;
    public static final int INT32_MIN = Integer.MIN_VALUE;

    // ==================== Decode 工具（按小端）====================

    public static int readFixed32LittleEndian(byte[] buf, int offset) {
        return (buf[offset] & 0xFF)
                | ((buf[offset + 1] & 0xFF) << 8)
                | ((buf[offset + 2] & 0xFF) << 16)
                | ((buf[offset + 3] & 0xFF) << 24);
    }

    public static long readFixed64LittleEndian(byte[] buf, int offset) {
        long lo = readFixed32LittleEndian(buf, offset);
        long hi = readFixed32LittleEndian(buf, offset + 4);
        return (hi << 32) | (lo & 0xFFFFFFFFL);
    }

    public static void writeFixed32LittleEndian(byte[] buf, int offset, int value) {
        buf[offset]     = (byte) (value);
        buf[offset + 1] = (byte) (value >>> 8);
        buf[offset + 2] = (byte) (value >>> 16);
        buf[offset + 3] = (byte) (value >>> 24);
    }

    public static void writeFixed64LittleEndian(byte[] buf, int offset, long value) {
        writeFixed32LittleEndian(buf, offset, (int) (value & 0xFFFFFFFFL));
        writeFixed32LittleEndian(buf, offset + 4, (int) (value >>> 32));
    }
}
