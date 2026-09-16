package io.zifu.z.serialize.annotation;

/**
 * 字段在 wire format 上的物理类型。
 *
 * <p>物理类型 vs 逻辑类型：</p>
 * <ul>
 *   <li><b>物理类型</b>（本枚举）：决定字节布局（tag、长度前缀、wire 类型）</li>
 *   <li><b>逻辑类型</b>（Java 字段类型）：决定 Java 侧的读/写方法</li>
 * </ul>
 *
 * <p>物理类型决定 <b>跨语言兼容性</b>。逻辑类型只是 Java 侧的便利映射。</p>
 */
public enum FieldType {
    /** Varint 编码（int32, int64, uint32, uint64, bool, enum）。 */
    VARINT(0),

    /** 64-bit fixed (double, int64 fixed)。 */
    FIXED64(1),

    /** Length-delimited 字节序列（string, bytes, embedded message, packed repeated）。 */
    LENGTH_DELIMITED(2),

    /** 32-bit fixed (float, int32 fixed)。 */
    FIXED32(5);

    private final int wireValue;

    FieldType(int wireValue) {
        this.wireValue = wireValue;
    }

    /** Wire format 上的数字（与 Protobuf 兼容）。 */
    public int wireValue() {
        return wireValue;
    }

    public static FieldType fromWireValue(int v) {
        switch (v) {
            case 0: return VARINT;
            case 1: return FIXED64;
            case 2: return LENGTH_DELIMITED;
            case 5: return FIXED32;
            default: throw new IllegalArgumentException("Unknown wire type: " + v);
        }
    }
}
