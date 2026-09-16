package com.zifang.util.xml.exception;

/**
 * XML 解析异常。
 *
 * @author zifang
 */

/**
 * XmlParseException类。
 */
public class XmlParseException extends RuntimeException {

    /**
     * XmlParseException方法。
     * * @param message String类型参数
     */
    public XmlParseException(String message) {
        super(message);
    }

    /**
     * 带原始异常的构造方法。
     *
     * @param message 异常描述
     * @param cause   原始异常
     */
    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
