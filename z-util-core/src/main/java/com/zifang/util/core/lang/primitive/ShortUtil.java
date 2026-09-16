package com.zifang.util.core.lang.primitive;

/**
 * @author: zifang
 * @time: 2021-12-02 13:09:00
 * @description: short util
 * @version: JDK 1.8
 */
public class ShortUtil {

    /**
     * parseShort方法。
     * * @param object Object类型参数
     *
     * @return static Short类型返回值
     */
    public static Short parseShort(Object object) {
        if (null == object) {
            return null;
        }
        return Short.parseShort(object.toString());
    }

    /**
     * parseShortOrDefault方法。
     * * @param object Object类型参数
     *
     * @param defaultValue short类型参数
     * @return static Short类型返回值
     */
    public static Short parseShortOrDefault(Object object, Short defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        return Short.parseShort(object.toString());
    }

}
