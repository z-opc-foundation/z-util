package com.zifang.util.expr.sql.function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zifang.util.core.json.JsonMapperFactory;
import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.annotation.SqlFunction;

import java.util.Iterator;
import java.util.Map;

/**
 * 内置 JSON 函数（MySQL 语义子集）。
 * <ul>
 *   <li>{@code JSON_EXTRACT(doc, '$.a.b[0]')}：按 path 取值；标量返回 Java 值，
 *       对象/数组返回紧凑 JSON 文本，path 未命中返回 NULL。</li>
 *   <li>{@code JSON_CONTAINS(target, candidate[, path])}：包含判定，返回 1/0。
 *       数组候选要求元素逐一被包含，对象候选要求键值逐一被包含，
 *       数值按十进制值比较（1 与 1.0 等价）。</li>
 * </ul>
 * 依赖 z-util-core 的共享 Jackson ObjectMapper（expr-sql 已传递依赖 z-util-core）。
 */
public final class SqlJsonFunctions {

    private static final ObjectMapper MAPPER = JsonMapperFactory.getDefault();

    private SqlJsonFunctions() {
    }

    @SqlFunction("JSON_EXTRACT")
    public static Object json_extract(Map<String, Object> row, Object doc, Object path) {
        if (doc == null || path == null) {
            return null;
        }
        JsonNode node = selectPath(readTree(doc), path.toString());
        return toJavaValue(node);
    }

    @SqlFunction("JSON_CONTAINS")
    public static Object json_contains(Map<String, Object> row, Object target, Object candidate, Object... path) {
        if (target == null || candidate == null) {
            return null;
        }
        JsonNode targetNode = readTree(target);
        if (path != null && path.length > 0 && path[0] != null) {
            targetNode = selectPath(targetNode, path[0].toString());
            if (targetNode == null) {
                return null; // MySQL：path 未命中返回 NULL
            }
        }
        JsonNode candidateNode = readTree(candidate);
        return contains(targetNode, candidateNode) ? 1L : 0L;
    }

    // ===================== 内部实现 =====================

    private static JsonNode readTree(Object v) {
        try {
            if (v instanceof JsonNode) {
                return (JsonNode) v;
            }
            if (v instanceof String || v instanceof Character) {
                return MAPPER.readTree(v.toString());
            }
            return MAPPER.valueToTree(v);
        } catch (Exception e) {
            throw new SqlException("非法 JSON 文档: " + v, e);
        }
    }

    /**
     * 解析 MySQL 风格 path：{@code $} 开头，{@code .key} / {@code ."quoted key"} / {@code [idx]} 链式下钻。
     * 未命中返回 null（区别于 JSON null 值——那种情况返回 NullNode）。
     */
    private static JsonNode selectPath(JsonNode root, String path) {
        String p = path.trim();
        if (!p.startsWith("$")) {
            throw new SqlException("非法 JSON path（须以 $ 开头）: " + path);
        }
        JsonNode cur = root;
        int i = 1;
        while (i < p.length()) {
            char c = p.charAt(i);
            if (c == '.') {
                i++;
                String key;
                if (i < p.length() && (p.charAt(i) == '"' || p.charAt(i) == '\'')) {
                    char quote = p.charAt(i++);
                    int start = i;
                    while (i < p.length() && p.charAt(i) != quote) {
                        i++;
                    }
                    if (i >= p.length()) {
                        throw new SqlException("JSON path 引号未闭合: " + path);
                    }
                    key = p.substring(start, i);
                    i++;
                } else {
                    int start = i;
                    while (i < p.length() && p.charAt(i) != '.' && p.charAt(i) != '[') {
                        i++;
                    }
                    key = p.substring(start, i);
                }
                if (key.isEmpty()) {
                    throw new SqlException("JSON path 含空 key: " + path);
                }
                cur = cur.get(key);
                if (cur == null) {
                    return null;
                }
            } else if (c == '[') {
                int close = p.indexOf(']', i);
                if (close < 0) {
                    throw new SqlException("JSON path 方括号未闭合: " + path);
                }
                String idx = p.substring(i + 1, close).trim();
                i = close + 1;
                int n;
                try {
                    n = Integer.parseInt(idx);
                } catch (NumberFormatException e) {
                    throw new SqlException("JSON path 数组下标非法: " + path);
                }
                if (!cur.isArray() || n < 0 || n >= cur.size()) {
                    return null;
                }
                cur = cur.get(n);
            } else {
                throw new SqlException("JSON path 语法不支持（位置 " + i + "）: " + path);
            }
        }
        return cur;
    }

    private static Object toJavaValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isValueNode()) {
            if (node.isTextual()) {
                return node.textValue();
            }
            if (node.isBoolean()) {
                return node.booleanValue();
            }
            if (node.isNumber()) {
                return node.numberValue();
            }
            return node.asText();
        }
        try {
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            throw new SqlException("JSON 序列化失败", e);
        }
    }

    private static boolean contains(JsonNode target, JsonNode candidate) {
        if (target.isObject() && candidate.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = candidate.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode tv = target.get(field.getKey());
                if (tv == null || !contains(tv, field.getValue())) {
                    return false;
                }
            }
            return true;
        }
        if (target.isArray()) {
            for (JsonNode t : target) {
                if (contains(t, candidate)) {
                    return true;
                }
            }
            return false;
        }
        return jsonEquals(target, candidate);
    }

    private static boolean jsonEquals(JsonNode a, JsonNode b) {
        if (a.isNumber() && b.isNumber()) {
            return a.decimalValue().stripTrailingZeros().compareTo(b.decimalValue().stripTrailingZeros()) == 0;
        }
        return a.equals(b);
    }
}
