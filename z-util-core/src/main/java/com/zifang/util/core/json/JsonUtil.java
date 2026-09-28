package com.zifang.util.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * JSON 规范化与等价比较工具（基于 {@link JsonMapperFactory} 的共享 ObjectMapper）。
 * <p>
 * 典型用途：签名前的 key 定序、配置快照比对、契约回归断言。
 * 与 {@code com.zifang.util.core.encrypt.ApiSignUtil#buildSortedQueryString} 的区别是：
 * 后者只对扁平 Map 的 query string 排序，本类处理任意深度嵌套的 JSON。
 */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = JsonMapperFactory.getDefault();

    private JsonUtil() {
    }

    /**
     * 递归按 key 自然序（大小写敏感）排序 JSON 对象的字段，数组内元素同样递归处理。
     * 输出为紧凑格式，同一输入恒定得到同一输出。
     *
     * @param json JSON 字符串
     * @return 排序后的紧凑 JSON 字符串；入参为 null 或空白时返回原值
     * @throws IllegalArgumentException json 不是合法 JSON 时抛出
     */
    public static String sortKeys(String json) {
        JsonNode sorted = sortNode(read(json));
        return write(sorted);
    }

    /**
     * 将对象先序列化再规范化，省去调用方自己拼字符串。
     *
     * @param value 任意可被 Jackson 序列化的对象，为 null 时返回 {@code "null"}
     * @return 规范化（字段定序 + 紧凑）后的 JSON 字符串，可直接用作签名串或快照指纹
     */
    public static String canonicalValue(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return sortKeys(MAPPER.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize value for canonical json", e);
        }
    }

    /**
     * 递归将对象字段名的首字母大写（数组元素同样递归处理）。
     *
     * @param json JSON 字符串
     * @return key 首字母大写后的紧凑 JSON 字符串
     */
    public static String upperFirstKeys(String json) {
        return write(upperFirstNode(read(json)));
    }

    /**
     * 判断两段 JSON 是否语义等价：对象忽略 key 顺序，数组有序比较，数值按十进制值比较
     * （因此 {@code 1} 与 {@code 1.00} 等价），字符串不做数字强制转换。
     *
     * @param left  左 JSON 字符串，可为 null
     * @param right 右 JSON 字符串，可为 null
     * @return 语义等价返回 true；两段均为 null 时返回 true
     */
    public static boolean deepEquals(String left, String right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return nodeEquals(read(left), read(right));
    }

    private static JsonNode read(String json) {
        if (json == null) {
            return null;
        }
        if (json.trim().isEmpty()) {
            throw new IllegalArgumentException("blank json input");
        }
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid json: " + abbreviate(json), e);
        }
    }

    private static String write(JsonNode node) {
        if (node == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to write json node", e);
        }
    }

    /**
     * 递归排序节点：对象字段按自然序重排，数组逐元素递归排序后保持原顺序。
     */
    private static JsonNode sortNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isObject()) {
            ObjectNode out = MAPPER.createObjectNode();
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            Collections.sort(names);
            for (String name : names) {
                out.set(name, sortNode(node.get(name)));
            }
            return out;
        }
        if (node.isArray()) {
            ArrayNode out = MAPPER.createArrayNode();
            for (JsonNode child : node) {
                out.add(sortNode(child));
            }
            return out;
        }
        return node;
    }

    /**
     * 递归将对象 key 首字母大写。
     */
    private static JsonNode upperFirstNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isObject()) {
            ObjectNode out = MAPPER.createObjectNode();
            Iterator<String> fields = node.fieldNames();
            while (fields.hasNext()) {
                String name = fields.next();
                out.set(upperFirst(name), upperFirstNode(node.get(name)));
            }
            return out;
        }
        if (node.isArray()) {
            ArrayNode out = MAPPER.createArrayNode();
            for (JsonNode child : node) {
                out.add(upperFirstNode(child));
            }
            return out;
        }
        return node;
    }

    private static String upperFirst(String name) {
        if (name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * 语义等价比较：数值走 BigDecimal，数组按序，对象按字段集合，其余交给 Jackson equals。
     */
    private static boolean nodeEquals(JsonNode left, JsonNode right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        if (left.isNumber() && right.isNumber()) {
            BigDecimal a = left.decimalValue();
            BigDecimal b = right.decimalValue();
            return a.compareTo(b) == 0;
        }
        if (left.isArray()) {
            if (!right.isArray() || left.size() != right.size()) {
                return false;
            }
            for (int i = 0; i < left.size(); i++) {
                if (!nodeEquals(left.get(i), right.get(i))) {
                    return false;
                }
            }
            return true;
        }
        if (left.isObject()) {
            if (!right.isObject() || left.size() != right.size()) {
                return false;
            }
            Iterator<String> fields = left.fieldNames();
            while (fields.hasNext()) {
                String name = fields.next();
                if (!right.has(name)) {
                    return false;
                }
                if (!nodeEquals(left.get(name), right.get(name))) {
                    return false;
                }
            }
            return true;
        }
        if (left.isNull() && right.isNull()) {
            return true;
        }
        return left.equals(right);
    }

    private static String abbreviate(String value) {
        return value.length() <= 64 ? value : value.substring(0, 64) + "...";
    }
}
