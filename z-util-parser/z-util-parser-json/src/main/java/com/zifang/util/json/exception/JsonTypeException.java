package com.zifang.util.json.exception;

/**
 * JSON类型异常，当JSON数据类型不匹配时抛出。
 *
 * @author zifang
 */

/**
 * JsonTypeException类。
 */
public class JsonTypeException extends RuntimeException {

    /**
     * 构造一个JSON类型异常。
     *
     * @param message 异常消息
     */
    /**
     * JsonTypeException方法。
     * * @param message String类型参数
     */
    public JsonTypeException(String message) {
        super(message);
    }
}
