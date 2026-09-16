package com.zifang.util.yaml.dsl;

import com.zifang.util.yaml.YamlParser;
import com.zifang.util.yaml.exception.YamlParseException;
import com.zifang.util.yaml.model.YamlArray;
import com.zifang.util.yaml.model.YamlMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * YAMLPath 解析器，支持类似 JsonPath 的路径表达式查询 YAML 数据。
 *
 * @author zifang
 */
/**
 * YamlPathParser类。
 */

/**
 * YamlPathParser类。
 */
public class YamlPathParser {

    /**
     * YamlPathParser方法。
     */
    /**
     * YamlPathParser方法。
     */
    public YamlPathParser() {
    }

    /**
     * 使用 YAMLPath 表达式查询 YAML 数据。
     * <p>
     * 支持的路径语法：
     * <ul>
     *   <li>{@code $.key} - 获取根对象下指定键的值</li>
     *   <li>{@code $.key.subkey} - 获取嵌套键的值</li>
     *   <li>{@code $.array[0]} - 获取数组指定索引的元素</li>
     *   <li>{@code $.array[*]} - 获取数组所有元素</li>
     *   <li>{@code $..key} - 递归搜索所有层级的指定键</li>
     * </ul>
     *
     * @param yaml YAML 字符串
     * @param path YAMLPath 表达式，例如 "$.config.database.host"、"$.items[0]"
     * @return 匹配结果的列表
     */
    /**
     * query方法。
     *      * @param yaml String类型参数
     * @param path String类型参数
     * @return List<Object>类型返回值
     */
    /**
     * query方法。
     * * @param yaml String类型参数
     *
     * @param path String类型参数
     * @return List<Object>类型返回值
     */
    public List<Object> query(String yaml, String path) {
        if (yaml == null || path == null) {
            return new ArrayList<>();
        }

        Object root;
        try {
            root = new YamlParser().parse(yaml);
        } catch (Exception e) {
            throw new YamlParseException("Failed to parse YAML for query: " + e.getMessage(), e);
        }

        if (root == null) {
            return new ArrayList<>();
        }

        // 去掉开头的 $ 和 .
        String normalized = path.trim();
        if (normalized.startsWith("$")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }

        List<Object> results = new ArrayList<>();
        queryStep(root, normalized, results);
        return results;
    }

    /**
     * 单步解析 path 片段，递归处理。
     * 每次处理一个 .key 或 [index] 或 [*] 或 .. 开头的部分
     */
    private void queryStep(Object current, String path, List<Object> results) {
        if (current == null || path == null || path.isEmpty()) {
            if (current != null && (path == null || path.isEmpty())) {
                results.add(current);
            }
            return;
        }

        String remaining;
        Object next;

        // 处理 $..key 递归下降
        if (path.startsWith("..")) {
            String searchKey = parseNextKey(path.substring(2));
            remaining = path.substring(2 + searchKey.length());
            // 递归搜索当前节点所有值
            searchRecursive(current, searchKey, remaining, results);
            return;
        }

        // 处理 [index]
        if (path.startsWith("[")) {
            int closeBracket = path.indexOf(']');
            if (closeBracket == -1) {
                return;
            }
            String indexStr = path.substring(1, closeBracket);
            remaining = path.substring(closeBracket + 1);
            if (remaining.startsWith(".")) {
                remaining = remaining.substring(1);
            }

            if ("*".equals(indexStr)) {
                // 通配符，遍历所有元素
                if (current instanceof YamlArray) {
                    YamlArray arr = (YamlArray) current;
                    for (Object item : arr) {
                        queryStep(item, remaining, results);
                    }
                } else if (current instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) current;
                    for (Object item : list) {
                        queryStep(item, remaining, results);
                    }
                }
            } else {
                // 数字索引
                try {
                    int index = Integer.parseInt(indexStr);
                    next = getIndex(current, index);
                    if (next != null) {
                        queryStep(next, remaining, results);
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效索引
                }
            }
            return;
        }

        // 处理 .key
        if (path.startsWith(".")) {
            int dotIdx = path.indexOf('.', 1);
            int bracketIdx = path.indexOf('[');
            int endIdx;
            if (dotIdx == -1 && bracketIdx == -1) {
                endIdx = path.length();
            } else if (dotIdx == -1) {
                endIdx = bracketIdx;
            } else if (bracketIdx == -1) {
                endIdx = dotIdx;
            } else {
                endIdx = Math.min(dotIdx, bracketIdx);
            }
            String key = path.substring(1, endIdx);
            remaining = path.substring(endIdx);
            if (remaining.startsWith(".")) {
                remaining = remaining.substring(1);
            }

            next = getKey(current, key);
            if (next != null) {
                queryStep(next, remaining, results);
            }
            return;
        }

        // 处理无前缀的 key（path 直接是 key）
        int dotIdx = path.indexOf('.');
        int bracketIdx = path.indexOf('[');
        String key;
        if (dotIdx == -1 && bracketIdx == -1) {
            key = path;
            remaining = "";
        } else if (dotIdx == -1) {
            key = path.substring(0, bracketIdx);
            remaining = path.substring(bracketIdx);
        } else if (bracketIdx == -1) {
            key = path.substring(0, dotIdx);
            remaining = path.substring(dotIdx);
        } else {
            int endIdx = Math.min(dotIdx, bracketIdx);
            key = path.substring(0, endIdx);
            remaining = path.substring(endIdx);
        }
        if (remaining.startsWith(".")) {
            remaining = remaining.substring(1);
        }

        next = getKey(current, key);
        if (next != null) {
            queryStep(next, remaining, results);
        }
    }

    private String parseNextKey(String path) {
        if (path.isEmpty()) {
            return "";
        }
        if (path.startsWith("[")) {
            int closeBracket = path.indexOf(']');
            if (closeBracket == -1) return "";
            return path.substring(0, closeBracket + 1);
        }
        int dotIdx = path.indexOf('.');
        int bracketIdx = path.indexOf('[');
        if (dotIdx == -1 && bracketIdx == -1) {
            return path;
        } else if (dotIdx == -1) {
            return path.substring(0, bracketIdx);
        } else if (bracketIdx == -1) {
            return path.substring(0, dotIdx);
        } else {
            return path.substring(0, Math.min(dotIdx, bracketIdx));
        }
    }

    private void searchRecursive(Object current, String searchKey, String remaining, List<Object> results) {
        if (current == null) return;

        // 如果 searchKey 以 [ 开头，是数组索引或通配符
        if (searchKey.startsWith("[")) {
            if ("[*]".equals(searchKey)) {
                // 遍历所有数组元素
                if (current instanceof YamlArray) {
                    for (Object item : (YamlArray) current) {
                        if (!remaining.isEmpty()) {
                            queryStep(item, remaining, results);
                        } else {
                            results.add(item);
                        }
                        searchRecursive(item, "[*]", remaining, results);
                    }
                }
            } else {
                // 数组索引
                int idx = -1;
                try {
                    String indexStr = searchKey.substring(1, searchKey.length() - 1);
                    idx = Integer.parseInt(indexStr);
                } catch (Exception e) {
                    // ignore
                }
                if (idx >= 0) {
                    Object item = getIndex(current, idx);
                    if (item != null) {
                        if (!remaining.isEmpty()) {
                            queryStep(item, remaining, results);
                        } else {
                            results.add(item);
                        }
                    }
                }
            }
            return;
        }

        // 搜索所有键匹配 searchKey 的值
        if (current instanceof YamlMap) {
            YamlMap map = (YamlMap) current;
            if (map.containsKey(searchKey)) {
                Object val = map.get(searchKey);
                if (!remaining.isEmpty()) {
                    queryStep(val, remaining, results);
                } else {
                    results.add(val);
                }
            }
            // 继续递归搜索所有值
            for (Object val : map.values()) {
                searchRecursive(val, searchKey, remaining, results);
            }
        } else if (current instanceof YamlArray) {
            for (Object item : (YamlArray) current) {
                searchRecursive(item, searchKey, remaining, results);
            }
        } else if (current instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) current;
            for (Object item : list) {
                searchRecursive(item, searchKey, remaining, results);
            }
        }
    }

    private Object getKey(Object current, String key) {
        if (current instanceof YamlMap) {
            return ((YamlMap) current).get(key);
        } else if (current instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) current;
            return map.get(key);
        }
        return null;
    }

    private Object getIndex(Object current, int index) {
        if (current instanceof YamlArray) {
            YamlArray arr = (YamlArray) current;
            if (index >= 0 && index < arr.size()) {
                return arr.get(index);
            }
        } else if (current instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) current;
            if (index >= 0 && index < list.size()) {
                return list.get(index);
            }
        }
        return null;
    }
}
