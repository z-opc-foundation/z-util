package com.zifang.util.core.lang.serialize;

/**
 * ZSerializer 抛出的序列化/反序列化错误。
 *
 * @author zifang
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
