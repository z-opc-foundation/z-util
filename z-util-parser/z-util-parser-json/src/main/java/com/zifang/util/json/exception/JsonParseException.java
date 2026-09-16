package com.zifang.util.json.exception;

/**
 * JSON解析异常，当JSON字符串格式不正确时抛出。
 *
 * @author zifang
 */

/**
 * JsonParseException类。
 */
public class JsonParseException extends RuntimeException {

    /**
     * 构造一个JSON解析异常。
     *
     * @param message 异常消息
     */
    /**
     * JsonParseException方法。
     * * @param message String类型参数
     */
    public JsonParseException(String message) {
        super(message);
    }
}
