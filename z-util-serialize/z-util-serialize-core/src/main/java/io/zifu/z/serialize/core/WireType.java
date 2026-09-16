package io.zifu.z.serialize.core;

/**
 * Wire format 类型（与 Protobuf 兼容）。
 *
 * <p>每个字段以 tag 起始：{@code (field_id << 3) | wire_type}，
 * wire_type 决定如何解析字段负载。</p>
 *
 * <p><b>跨语言契约：</b>任何语言实现都必须遵守 wire_type 编码，
 * 否则消息无法互通。详见 {@code docs/wire-format.md}。</p>
 */
public final class WireType {
    private WireType() {}

    public static final int VARINT          = 0;
    public static final int FIXED64         = 1;
    public static final int LENGTH_DELIMITED = 2;
    /** wire type 3 (start group) and 4 (end group) 已被 Protobuf 标记 deprecated，不使用 */
    public static final int FIXED32         = 5;

    /** wire type 是否合法。 */
    public static boolean isValid(int wireType) {
        return wireType == 0 || wireType == 1 || wireType == 2 || wireType == 5;
    }

    public static String nameOf(int wireType) {
        switch (wireType) {
            case VARINT:           return "VARINT";
            case FIXED64:          return "FIXED64";
            case LENGTH_DELIMITED: return "LENGTH_DELIMITED";
            case FIXED32:          return "FIXED32";
            default: return "UNKNOWN(" + wireType + ")";
        }
    }
}
