package com.zifang.util.json.define;

/**
 * JSON解析器接口，定义解析JSON字符串为JSON对象或JSON数组的操作规范。
 *
 * @author zifang
 * @see com.zifang.util.json.parser.Parser
 */

/**
 * IJsonParser接口。
 */
public interface IJsonParser {

    /**
     * 将JSON字符串解析为JSON对象。
     *
     * @param json JSON字符串
     * @return 解析后的IJsonObject实例
     */
    IJsonObject parserJsonObject(String json);

    /**
     * 将JSON字符串解析为JSON数组。
     *
     * @param json JSON字符串
     * @return 解析后的IJsonArray实例
     */
    IJsonArray parserJsonArray(String json);

}
