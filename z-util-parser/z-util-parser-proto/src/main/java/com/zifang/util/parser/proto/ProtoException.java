package com.zifang.util.parser.proto;

/**
 * Proto 解析异常，当 .proto 文件格式非法时抛出。
 *
 * @author zifang
 */

/**
 * ProtoException类。
 */
public class ProtoException extends RuntimeException {

    /**
     * ProtoException方法。
     * * @param message String类型参数
     */
    public ProtoException(String message) {
        super(message);
    }

    /**
     * ProtoException方法。
     * * @param message String类型参数
     *
     * @param cause Throwable类型参数
     */
    public ProtoException(String message, Throwable cause) {
        super(message, cause);
    }
}
