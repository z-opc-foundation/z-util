package com.zifang.util.parser.toml;

/**
 * TOML 解析异常
 */

/**
 * TomlException类。
 */
public class TomlException extends RuntimeException {

    /**
     * TomlException方法。
     * * @param message String类型参数
     */
    public TomlException(String message) {
        super(message);
    }

    /**
     * TomlException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public TomlException(String message, Throwable cause) {
        super(message, cause);
    }
}
