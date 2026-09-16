package com.zifang.util.core.lang.exception;

/**
 * 类型转换异常类，继承自 RuntimeException。
 * <p>
 * 用于表示类型转换过程中发生的异常，例如将字符串转换为数字时格式不正确，
 * 或将对象转换为目标类型时类型不匹配。
 *
 * @author zifang
 */
public class ConversionException extends RuntimeException {

    /**
     * 使用转换失败的值和原始转换异常创建类型转换异常。
     *
     * @param value 转换失败的值
     * @param nfex  原始的 NumberFormatException
     */
    public ConversionException(Object value, NumberFormatException nfex) {
        super("find exception when convert " + value, nfex);
    }
}
