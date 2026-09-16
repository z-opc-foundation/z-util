package com.zifang.util.core.lang.primitive;

/**
 * @author: zifang
 * @time: 2021-12-16 20:06:00
 * @description: boolean util
 * @version: JDK 1.8
 */
public class BooleanUtil {

    /**
     * parseBoolean方法。
     * * @param object Object类型参数
     *
     * @return static Boolean类型返回值
     */
    public static Boolean parseBoolean(Object object) {
        if (null == object) {
            return null;
        }
        return Boolean.parseBoolean(object.toString());
    }

    /**
     * parseBooleanOrDefault方法。
     * * @param object Object类型参数
     *
     * @param defaultValue boolean类型参数
     * @return static Boolean类型返回值
     */
    public static Boolean parseBooleanOrDefault(Object object, Boolean defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        return Boolean.parseBoolean(object.toString());
    }

    /**
     * @author: zifang
     * @description: Compzifang two {@code boolean} values
     * @description: 比较两个boolean值是否一致
     * @time: 2022-06-08 10:24:20
     * @params: [x, y] 值
     * @return: int 是否一致
     */
    public static int compare(final boolean x, final boolean y) {
        if (x == y) {
            return 0;
        }
        return x ? 1 : -1;
    }

}
