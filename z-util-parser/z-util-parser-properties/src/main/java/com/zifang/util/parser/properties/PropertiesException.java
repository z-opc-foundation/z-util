package com.zifang.util.parser.properties;

/**
 * Properties 解析异常，当 Properties 字符串格式非法时抛出。
 *
 * @author zifang
 */

/**
 * PropertiesException类。
 */
public class PropertiesException extends RuntimeException {

    /**
     * PropertiesException方法。
     * * @param message String类型参数
     */
    public PropertiesException(String message) {
        super(message);
    }

    /**
     * PropertiesException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public PropertiesException(String message, Throwable cause) {
        super(message, cause);
    }
}
