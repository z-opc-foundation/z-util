package com.zifang.util.core.lang.regex;

import java.util.regex.Pattern;

/**
 * 常用正则表达式模式定义。
 * <p>
 * 包含格式化说明符、浮点数等常见正则表达式常量。
 *
 * @author zifang
 * @see Pattern
 */
public class Patterns {

    public static String FORMAT_SPECIFIER = "%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    public static String FLOATING_POINT_NUMBER_FORMAT = "^[-\\+]?[.\\d]*$";

    /**
     * Pattern.compile方法。
     * * @param FORMAT_SPECIFIER Object类型参数
     *
     * @return static Pattern FORMAT_PATTERN =类型返回值
     */
    public static Pattern FORMAT_PATTERN = Pattern.compile(FORMAT_SPECIFIER);
    /**
     * Pattern.compile方法。
     * * @param FLOATING_POINT_NUMBER_FORMAT Object类型参数
     *
     * @return static Pattern FLOATING_POINT_NUMBER_PATTERN =类型返回值
     */
    public static Pattern FLOATING_POINT_NUMBER_PATTERN = Pattern.compile(FLOATING_POINT_NUMBER_FORMAT);
}
