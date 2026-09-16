package com.zifang.util.core.lang.converter.converters;

import com.zifang.util.core.lang.converter.IConverter;

/**
 * String 到 Integer 的转换器实现。
 *
 * @author zifang
 * @see IConverter
 */
public class StringIntegerConverter implements IConverter<String, Integer> {

    @Override
    /**
     * to方法。
     *      * @param value String类型参数
     * @return int类型返回值
     */
    public Integer to(String value) {
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value.trim());
    }

    /**
     * to方法。
     * * @param value String类型参数
     *
     * @param defaultValue int类型参数
     * @return int类型返回值
     */
    public Integer to(String value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}