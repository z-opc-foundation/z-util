package com.zifang.util.core.lang.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;

/**
 * 提供一些对象有效性校验的方法
 * 统一返回 校验错误/校验失败
 */
public class Checker {

    /**
     * 判断字符串是否是符合指定格式的时间
     *
     * @param date   待验证的日期字符串
     * @param format 日期格式（如 "yyyy-MM-dd"）
     * @return 如果字符串符合指定格式返回true，否则返回false
     */
    public static boolean isDate(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断字符串有效性（不为null且不为空字符串）
     *
     * @param src 待验证的字符串
     * @return 如果字符串有效返回true，否则返回false
     */
    public static boolean valid(String src) {
        return !(src == null || src.trim().isEmpty());
    }

    /**
     * 判断一组字符串是否全部有效
     *
     * @param src 字符串数组
     * @return 如果所有字符串都有效返回true，否则返回false
     */
    public static boolean valid(String[] src) {
        for (String s : src) {
            if (!valid(s)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 判断一个对象是否有效（不为null）
     *
     * @param obj 待验证的对象
     * @return 如果对象不为null返回true，否则返回false
     */
    public static boolean valid(Object obj) {
        return !(null == obj);
    }

    /**
     * 判断一组对象是否有效（不为null且数组长度大于0）
     *
     * @param objs 对象数组
     * @return 如果数组不为null且长度大于0返回true，否则返回false
     */
    public static boolean valid(Object[] objs) {
        return objs != null && objs.length != 0;
    }

    /**
     * 判断集合的有效性（不为null且不为空）
     *
     * @param col 待验证的集合
     * @return 如果集合不为null且不为空返回true，否则返回false
     */
    public static boolean valid(Collection<?> col) {
        return !(col == null || col.isEmpty());
    }

    /**
     * 判断一组集合是否全部有效
     *
     * @param cols 集合数组
     * @return 如果所有集合都有效返回true，否则返回false
     */
    public static boolean valid(Collection<?>... cols) {
        for (Collection<?> c : cols) {
            if (!valid(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断map是否有效（不为null且不为空）
     *
     * @param map 待验证的map
     * @return 如果map不为null且不为空返回true，否则返回false
     */
    public static boolean valid(Map<?, ?> map) {
        return !(map == null || map.isEmpty());
    }

    /**
     * 判断一组map是否全部有效
     *
     * @param maps map数组
     * @return 如果所有map都有效返回true，否则返回false
     */
    public static boolean valid(Map<?, ?>... maps) {
        for (Map<?, ?> m : maps) {
            if (!valid(m)) {
                return false;
            }
        }
        return true;
    }
}
