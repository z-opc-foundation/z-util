package com.zifang.util.numpy;

/**
 * Data types supported by NdArray
 */
public enum DType {
    INT8("int8"),
    INT16("int16"),
    INT32("int32"),
    INT64("int64"),
    FLOAT32("float32"),
    FLOAT64("float64"),
    BOOL("bool"),
    STRING("string"),
    OBJECT("object");

    private final String name;

    DType(String name) {
        this.name = name;
    }

    /**
     * fromClass方法。
     * * @param cls Class?类型参数
     *
     * @return static DType类型返回值
     */
    public static DType fromClass(Class<?> cls) {
        if (cls == byte.class || cls == Byte.class) return INT8;
        if (cls == short.class || cls == Short.class) return INT16;
        if (cls == int.class || cls == Integer.class) return INT32;
        if (cls == long.class || cls == Long.class) return INT64;
        if (cls == float.class || cls == Float.class) return FLOAT32;
        if (cls == double.class || cls == Double.class) return FLOAT64;
        if (cls == boolean.class || cls == Boolean.class) return BOOL;
        if (cls == String.class) return STRING;
        return OBJECT;
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
     * toClass方法。
     *
     * @return Class<?>类型返回值
     */
    public Class<?> toClass() {
        switch (this) {
            case INT8:
                return byte.class;
            case INT16:
                return short.class;
            case INT32:
                return int.class;
            case INT64:
                return long.class;
            case FLOAT32:
                return float.class;
            case FLOAT64:
                return double.class;
            case BOOL:
                return boolean.class;
            case STRING:
                return String.class;
            default:
                return Object.class;
        }
    }
}
