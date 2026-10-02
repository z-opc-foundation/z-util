package com.zifang.util.parser.ini;

/**
 * INI 文件解析异常
 */

/**
 * IniException类。
 */
public class IniException extends RuntimeException {

    /**
     * IniException方法。
     * * @param message String类型参数
     */
    public IniException(String message) {
        super(message);
    }

    /**
     * IniException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public IniException(String message, Throwable cause) {
        super(message, cause);
    }
}
