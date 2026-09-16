package com.zifang.util.core.lang.serialize;

/**
 * ZSerializer 协议中支持的类型标识（type id）。
 * <p>
 * 每个被序列化的对象前置 1 字节类型标签，然后是类型特定的负载。
 * <p>
 * 自定义类型请使用 {@link #CUSTOM}，并在 SPI 中注册对应编解码器。
 *
 * @author zifang
 */
public final class TypeId {

    private TypeId() {
    }

    public static final byte NULL             = 0x00;  // null
    public static final byte BOOLEAN          = 0x01;  // boolean: 1 byte
    public static final byte BYTE             = 0x02;  // byte: 1 byte
    public static final byte SHORT            = 0x03;  // short: 2 bytes big-endian
    public static final byte INT              = 0x04;  // int: 4 bytes big-endian
    public static final byte LONG             = 0x05;  // long: 8 bytes big-endian
    public static final byte FLOAT            = 0x06;  // float: 4 bytes IEEE-754
    public static final byte DOUBLE           = 0x07;  // double: 8 bytes IEEE-754
    public static final byte CHAR             = 0x08;  // char: 2 bytes UTF-16
    public static final byte STRING           = 0x09;  // String: varint length + UTF-8 bytes
    public static final byte BYTE_ARRAY       = 0x0A;  // byte[]: varint length + bytes
    public static final byte BOOLEAN_ARRAY    = 0x0B;  // boolean[]: varint length + N bytes
    public static final byte INT_ARRAY        = 0x0C;  // int[]: varint length + 4N bytes
    public static final byte LONG_ARRAY       = 0x0D;  // long[]: varint length + 8N bytes
    public static final byte DOUBLE_ARRAY     = 0x0E;  // double[]: varint length + 8N bytes
    public static final byte STRING_ARRAY     = 0x0F;  // String[]: varint length + N strings
    public static final byte OBJECT_ARRAY     = 0x10;  // Object[]: varint length + N objects
    public static final byte ARRAYLIST        = 0x20;  // ArrayList: varint size + N objects
    public static final byte HASHMAP          = 0x21;  // HashMap: varint size + N entries
    public static final byte LINKEDHASHMAP    = 0x22;  // LinkedHashMap: 同 HashMap
    public static final byte HASHSET          = 0x23;  // HashSet: varint size + N objects
    public static final byte LINKEDHASHSET    = 0x24;  // LinkedHashSet: 同 HashSet
    public static final byte TREEMAP          = 0x25;  // TreeMap (Comparable keys)
    public static final byte TREESET          = 0x26;  // TreeSet (Comparable elements)
    public static final byte DATE             = 0x30;  // Date: 8 bytes (long millis)
    public static final byte LOCAL_DATE       = 0x31;  // LocalDate: 3 varints (y/m/d)
    public static final byte LOCAL_TIME       = 0x32;  // LocalTime: 4 varints (h/m/s/nanos)
    public static final byte LOCAL_DATETIME   = 0x33;  // LocalDateTime: LocalDate + LocalTime
    public static final byte INSTANT          = 0x34;  // Instant: 8 bytes epoch seconds + 4 bytes nanos
    public static final byte UUID             = 0x35;  // UUID: 16 bytes (mostSig + leastSig)
    public static final byte BIGDECIMAL       = 0x36;  // BigDecimal: varint scale + unscaledBytes
    public static final byte BIGINTEGER       = 0x37;  // BigInteger: varint byteLen + magnitude bytes
    public static final byte CLASS            = 0x40;  // Class: String className
    public static final byte ENUM             = 0x41;  // Enum: String className + String name
    public static final byte OPTIONAL         = 0x50;  // Optional: bool present + value
    public static final byte ENUM_MAP         = 0x60;  // EnumMap: Class + N entries
    public static final byte ENUM_SET         = 0x61;  // EnumSet: Class + N elements

    // User-defined: 0x80 ~ 0xFF, paired with SPI Serializer&lt;T&gt;
    public static final byte CUSTOM           = (byte) 0x80;

    /**
     * 是否为内建类型（非 SPI）。
     */
    public static boolean isBuiltin(byte typeId) {
        return typeId >= 0 && typeId < (byte) 0x80;
    }
}
