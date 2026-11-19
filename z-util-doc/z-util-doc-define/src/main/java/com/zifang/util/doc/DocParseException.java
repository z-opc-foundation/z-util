package com.zifang.util.doc;

/** 解析失败或格式不支持时抛出。 */
public class DocParseException extends RuntimeException {

    public DocParseException(String message) {
        super(message);
    }

    public DocParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
