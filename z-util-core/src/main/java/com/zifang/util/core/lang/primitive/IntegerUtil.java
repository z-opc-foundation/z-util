package com.zifang.util.core.lang.primitive;

/**
 * @author: zifang
 * @time: 2021-12-02 12:56:00
 * @description: integer util
 * @version: JDK 1.8
 */
public class IntegerUtil {

    /**
     * parseInteger方法。
     * * @param object Object类型参数
     *
     * @return static Integer类型返回值
     */
    public static Integer parseInteger(Object object) {
        if (null == object) {
            return null;
        }
        return Integer.parseInt(object.toString());
    }

    /**
     * toIntegerOrNull方法。
     * 宽松安全转换：null、空串、"null"（忽略大小写）或无法解析的内容返回 null，不抛异常；
     * Number 类型直接取 intValue，数字字符串（含负号）按十进制解析。
     *
     * @param value 待转换值
     * @return Integer类型返回值，无法转换时为 null
     */
    public static Integer toIntegerOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String str = value.toString().trim();
        if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
            return null;
        }
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * parseIntegerOrDefault方法。
     * * @param object Object类型参数
     *
     * @param defaultValue int类型参数
     * @return static Integer类型返回值
     */
    public static Integer parseIntegerOrDefault(Object object, Integer defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        return Integer.parseInt(object.toString());
    }

    /**
     * saturatedCast方法。
     * * @param value long类型参数
     *
     * @return static int类型返回值
     */
    public static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return value < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) value;
        }
    }

}
