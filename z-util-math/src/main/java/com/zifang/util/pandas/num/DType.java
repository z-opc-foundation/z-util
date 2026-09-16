package com.zifang.util.pandas.num;

/**
 * 数据类型枚举 - 对标 numpy 的 dtype
 */
public enum DType {
    // 整数类型
    INT8("int8", 1, true),
    INT16("int16", 2, true),
    INT32("int32", 4, true),
    INT64("int64", 8, true),

    // 无符号整数
    UINT8("uint8", 1, false),
    UINT16("uint16", 2, false),
    UINT32("uint32", 4, false),
    UINT64("uint64", 8, false),

    // 浮点类型
    FLOAT32("float32", 4, false),
    FLOAT64("float64", 8, false),

    // 其他类型
    BOOL("bool", 1, false),
    STRING("string", 0, false),
    OBJECT("object", 0, false);

    private final String name;
    private final int size;
    private final boolean integer;

    DType(String name, int size, boolean integer) {
        this.name = name;
        this.size = size;
        this.integer = integer;
    }

    /**
     * 从字符串解析 dtype
     */
    public static DType fromString(String dtype) {
        if (dtype == null) return FLOAT64;
        try {
            return valueOf(dtype.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FLOAT64;
        }
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getSize方法。
     *
     * @return int类型返回值
     */
    public int getSize() {
        return size;
    }

    /**
     * isInteger方法。
     *
     * @return boolean类型返回值
     */
    public boolean isInteger() {
        return integer;
    }

    /**
     * isFloat方法。
     *
     * @return boolean类型返回值
     */
    public boolean isFloat() {
        return this == FLOAT32 || this == FLOAT64;
    }

    /**
     * isNumeric方法。
     *
     * @return boolean类型返回值
     */
    public boolean isNumeric() {
        return integer || isFloat();
    }
}
