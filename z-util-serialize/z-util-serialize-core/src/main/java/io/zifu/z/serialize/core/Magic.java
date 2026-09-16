package io.zifu.z.serialize.core;

/**
 * Z-Serialization Wire Format 常量。
 *
 * <pre>
 * [magic 2B]    = 0xBA 0xBE  (Binary Architecture Encoding)
 * [ver 1B]      = 0x01
 * [flags 1B]    = bit0: compressed | bit1: encrypted | bit2: indexed
 * [schemaId 4B] = little-endian int (来自 @ZMessage.id)
 * [schemaNameLen 1B] = UTF-8 长度（0 表示无）
 * [schemaName  N B] = UTF-8 字符串（仅 schemaNameLen > 0 时存在）
 * [bodyLen varint] = body 字节数（不含 header）
 * [body N B]    = tag + value 序列
 * [index]       = 可选字段索引
 * [footer 16B]  = HMAC-SHA256 truncated（仅加密）
 * </pre>
 *
 * <p><b>跨语言契约：</b>所有语言的实现必须严格遵守此布局。</p>
 */
public final class Magic {
    private Magic() {}

    /** 文件 magic（2 字节）。 */
    public static final int MAGIC_BYTE_0 = 0xBA;
    public static final int MAGIC_BYTE_1 = 0xBE;

    /** 当前 wire format 主版本（1 字节）。 */
    public static final byte WIRE_VERSION = 0x01;

    /** Header 固定部分字节数（magic 2 + ver 1 + flags 1 + schemaId 4 = 8）。 */
    public static final int HEADER_FIXED_LEN = 8;

    // Flags
    public static final byte FLAG_COMPRESSED = 0x01;
    public static final byte FLAG_ENCRYPTED  = 0x02;
    public static final byte FLAG_INDEXED    = 0x04;
}
