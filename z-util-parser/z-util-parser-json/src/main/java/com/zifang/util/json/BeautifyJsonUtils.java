package com.zifang.util.json;

import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * JSON 美化工具类，提供格式化的 JSON 输出功能。
 *
 * @author zifang
 */

/**
 * BeautifyJsonUtils类。
 */
public class BeautifyJsonUtils {

    private static final char SPACE_CHAR = ' ';

    private static final int INDENT_SIZE = 2;

    private static int callDepth = 0;

    /**
     * 美化输出 JsonObject，格式化缩进显示。
     *
     * @param jsonObject 待美化的 JsonObject 对象
     * @return 格式化后的 JSON 字符串
     */
    /**
     * beautify方法。
     * * @param jsonObject JsonObject类型参数
     *
     * @return static String类型返回值
     */
    public static String beautify(JsonObject jsonObject) {

        StringBuilder sb = new StringBuilder();
        sb.append(getIndentString());
        sb.append("{");
        callDepth++;

        List<Map.Entry<String, Object>> keyValues = jsonObject.getAllKeyValue();
        int size = keyValues.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<String, Object> keyValue = keyValues.get(i);

            String key = keyValue.getKey();
            Object value = keyValue.getValue();

            sb.append("\n");
            sb.append(getIndentString());
            sb.append("\"");
            sb.append(key);
            sb.append("\"");
            sb.append(": ");

            if (value instanceof JsonObject) {
                sb.append("\n");
                sb.append(beautify((JsonObject) value));
            } else if (value instanceof JsonArray) {
                sb.append("\n");
                sb.append(beautify((JsonArray) value));
            } else if (value instanceof String) {
                sb.append("\"");
                sb.append(value);
                sb.append("\"");
            } else {
                sb.append(value);
            }

            if (i < size - 1) {
                sb.append(",");
            }
        }

        callDepth--;
        sb.append("\n");
        sb.append(getIndentString());
        sb.append("}");

        return sb.toString();
    }

    /**
     * 美化输出 JsonArray，格式化缩进显示。
     *
     * @param jsonArray 待美化的 JsonArray 对象
     * @return 格式化后的 JSON 字符串
     */
    /**
     * beautify方法。
     * * @param jsonArray JsonArray类型参数
     *
     * @return static String类型返回值
     */
    public static String beautify(JsonArray jsonArray) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndentString());
        sb.append("[");
        callDepth++;

        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {

            sb.append("\n");

            Object ele = jsonArray.get(i);
            if (ele instanceof JsonObject) {
                sb.append(beautify((JsonObject) ele));
            } else if (ele instanceof JsonArray) {
                sb.append(beautify((JsonArray) ele));
            } else if (ele instanceof String) {
                sb.append(getIndentString());
                sb.append("\"");
                sb.append(ele);
                sb.append("\"");
            } else {
                sb.append(getIndentString());
                sb.append(ele);
            }

            if (i < size - 1) {
                sb.append(",");
            }
        }

        callDepth--;
        sb.append("\n");
        sb.append(getIndentString());
        sb.append("]");

        return sb.toString();
    }

    private static String getIndentString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < callDepth * INDENT_SIZE; i++) {
            sb.append(SPACE_CHAR);
        }
        return sb.toString();
    }
}
