package com.zifang.util.core.lang.primitive;

/**
 * @author: zifang
 * @time: 2021-12-05 21:03:00
 * @description: double util
 * @version: JDK 1.8
 */
public class DoubleUtil {

    /**
     * parseDouble方法。
     * * @param object Object类型参数
     *
     * @return static Double类型返回值
     */
    public static Double parseDouble(Object object) {
        if (null == object) {
            return null;
        }
        return Double.parseDouble(object.toString());
    }

    /**
     * parseDoubleOrDefault方法。
     * * @param object Object类型参数
     *
     * @param defaultValue double类型参数
     * @return static Double类型返回值
     */
    public static Double parseDoubleOrDefault(Object object, Double defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        return Double.parseDouble(object.toString());
    }

}
