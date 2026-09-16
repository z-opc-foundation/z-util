package com.zifang.util.xml.binding;

/**
 * XML 绑定异常（Bean ↔ XML 转换时抛出）。
 *
 * @author zifang
 */
public class XmlBindingException extends RuntimeException {

    /**
     * XmlBindingException方法。
     * * @param message String类型参数
     *
     * @param cause   Throwable类型参数
     */
    public XmlBindingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * XmlBindingException方法。
     * * @param message String类型参数
     */
    public XmlBindingException(String message) {
        super(message);
    }
}
