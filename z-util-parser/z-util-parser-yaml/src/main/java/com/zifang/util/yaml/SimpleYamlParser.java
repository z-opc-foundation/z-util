package com.zifang.util.yaml;

import com.zifang.util.yaml.model.YamlArray;
import com.zifang.util.yaml.model.YamlMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单 YAML 解析器（Java 8 兼容）。
 * 支持 key: value、嵌套映射、序列。
 * 专门解决 DynamicParser 的 G4 grammar 局限性。
 */
public class SimpleYamlParser {

    private static final Pattern LINE_PAIR = Pattern.compile("^([\\w-]+)\\s*:\\s*(.*)$");
    private static final Pattern NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern BOOL = Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NULL = Pattern.compile("^(null|~)$", Pattern.CASE_INSENSITIVE);

    public Object parse(String yaml) {
        if (yaml == null || yaml.trim().isEmpty()) return new YamlMap();
        String[] lines = yaml.split("\\r?\\n");
        if (lines.length == 0) return new YamlMap();

        // 检测是否为纯序列（每行以 - 开头）
        if (lines[0].trim().startsWith("-")) {
            return parseSeq(lines, 0);
        }

        // 否则当作映射解析
        int[] indentStack = new int[20];
        int stackDepth = 0;
        Object[] valueStack = new Object[20];
        Object current = new YamlMap();
        valueStack[0] = current;

        for (String line : lines) {
            String trimmed = line;
            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') indent++;
            trimmed = trimmed.substring(indent);

            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("#")) continue;

            // 顶层空映射
            if (trimmed.equals("{}")) {
                current = new YamlMap();
                stackDepth = 0;
                indentStack[0] = 0;
                valueStack[0] = current;
                continue;
            }

            Matcher m = LINE_PAIR.matcher(trimmed);
            if (!m.matches()) continue;

            String key = m.group(1);
            String rawVal = m.group(2);

            // 缩进减小：弹出栈直到当前缩进 <= 栈顶
            while (stackDepth > 0 && indent < indentStack[stackDepth - 1]) {
                stackDepth--;
            }
            current = (YamlMap) valueStack[stackDepth];

            if (rawVal.isEmpty()) {
                // 空值：先不加入 parent，创建子映射作为 pending，
                // 等下一行有内容再真正挂入 parent
                YamlMap child = new YamlMap();
                stackDepth++;
                indentStack[stackDepth] = indent;
                valueStack[stackDepth] = child;
                // 先占位，等子键加入后再 put
                ((YamlMap) current).put(key, child);
                continue;
            }

            String val = rawVal.trim();

            // 内联序列 {a, b, c}
            if (val.startsWith("{") && val.endsWith("}")) {
                YamlArray arr = new YamlArray();
                String inner = val.substring(1, val.length() - 1).trim();
                if (!inner.isEmpty()) {
                    for (String item : inner.split(",")) {
                        arr.add(parseScalar(item.trim()));
                    }
                }
                ((YamlMap) current).put(key, arr);
                continue;
            }

            // 字符串值（可能有空白）
            if (val.startsWith("\"") && val.endsWith("\"")) {
                ((YamlMap) current).put(key, val.substring(1, val.length() - 1));
            } else if (val.startsWith("'") && val.endsWith("'")) {
                ((YamlMap) current).put(key, val.substring(1, val.length() - 1));
            } else {
                ((YamlMap) current).put(key, parseScalar(val));
            }
        }
        return valueStack[0];
    }

    private YamlArray parseSeq(String[] lines, int start) {
        YamlArray arr = new YamlArray();
        for (int i = start; i < lines.length; i++) {
            String line = lines[i];
            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') indent++;
            String trimmed = line.substring(indent).trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("#")) continue;

            if (trimmed.startsWith("-")) {
                String item = trimmed.substring(1).trim();
                if (item.startsWith("\"")) {
                    arr.add(item.substring(1, item.length() - 1));
                } else if (item.startsWith("'")) {
                    arr.add(item.substring(1, item.length() - 1));
                } else {
                    arr.add(parseScalar(item));
                }
            }
        }
        return arr;
    }

    private Object parseScalar(String s) {
        if (s == null || s.isEmpty()) return s;
        if (NULL.matcher(s).matches()) return null;
        if (BOOL.matcher(s).matches()) return Boolean.parseBoolean(s.toLowerCase());
        if (NUMBER.matcher(s).matches()) {
            if (s.contains(".")) return Double.parseDouble(s);
            return Double.parseDouble(s); // YAML number → Double
        }
        return s;
    }

    public YamlMap parseMap(String yaml) {
        Object result = parse(yaml);
        if (result instanceof YamlMap) return (YamlMap) result;
        YamlMap map = new YamlMap();
        if (result != null) map.put("value", result);
        return map;
    }
}
