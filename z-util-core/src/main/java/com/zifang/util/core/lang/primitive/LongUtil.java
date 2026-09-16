package com.zifang.util.core.lang.primitive;

/**
 * @author: zifang
 * @time: 2021-12-02 12:57:00
 * @description: long util
 * @version: JDK 1.8
 */
public class LongUtil {

    /**
     * parseLong方法。
     * * @param object Object类型参数
     *
     * @return static Long类型返回值
     */
    public static Long parseLong(Object object) {
        if (null == object) {
            return null;
        }
        return Long.parseLong(object.toString());
    }

    /**
     * parseLongOrDefault方法。
     * * @param object Object类型参数
     *
     * @param defaultValue long类型参数
     * @return static Long类型返回值
     */
    public static Long parseLongOrDefault(Object object, Long defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        return Long.parseLong(object.toString());
    }

    /**
     * length方法。
     * * @param value long类型参数
     *
     * @return static int类型返回值
     */
    public static int length(Long value) {
        if (null == value) {
            return 0;
        }
        return String.valueOf(value).length();
    }

}
