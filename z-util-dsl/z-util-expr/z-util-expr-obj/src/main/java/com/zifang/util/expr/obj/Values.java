package com.zifang.util.expr.obj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 值模型与路径读写：对象语言世界里只有三种值 —— 行数组（二维表）、对象、标量。
 * <p>
 * 所有写操作都在沿路径的浅拷贝上进行，绝不改输入行：行数据来自 {@code Table.toMapList()}
 * 时可能与内存表共享实例，改脏一次就会污染后续所有查询。
 *
 * @author zifang
 */
final class Values {

    private Values() {
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> asRows(Object value, String op) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (!(value instanceof List)) {
            throw new ObjException(op + " 需要二维行数组作为输入, 实际是 " + typeOf(value)
                    + "; 上游若不是表, 先接一个 from / map / one");
        }
        List<?> list = (List<?>) value;
        List<Map<String, Object>> rows = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item == null) {
                continue;
            }
            if (!(item instanceof Map)) {
                throw new ObjException(op + " 的第 " + i + " 行不是对象, 实际是 " + typeOf(item));
            }
            rows.add((Map<String, Object>) item);
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    static List<Object> asList(Object value, String op) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (!(value instanceof List)) {
            throw new ObjException(op + " 需要数组作为输入, 实际是 " + typeOf(value));
        }
        return (List<Object>) value;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asObject(Object value, String op) {
        if (!(value instanceof Map)) {
            throw new ObjException(op + " 需要对象作为输入, 实际是 " + typeOf(value));
        }
        return new LinkedHashMap<>((Map<String, Object>) value);
    }

    static String required(Map<String, Object> step, String slot, String op) {
        Object v = step.get(slot);
        if (v == null) {
            throw new ObjException(op + " 缺少必填槽位 \"" + slot + "\": " + step.keySet());
        }
        return String.valueOf(v);
    }

    static String optional(Map<String, Object> step, String slot, String fallback) {
        Object v = step.get(slot);
        return v == null ? fallback : String.valueOf(v);
    }

    /** 槽位既可以写单字符串也可以写数组：{@code by: "region"} 与 {@code by: ["region","city"]} 同义 */
    static List<String> asStrings(Object value, String op) {
        List<String> out = new ArrayList<>();
        if (value == null) {
            return out;
        }
        if (value instanceof List) {
            for (Object v : (List<?>) value) {
                out.add(String.valueOf(v));
            }
            return out;
        }
        out.add(String.valueOf(value));
        return out;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object value, String op) {
        if (value == null) {
            return Collections.emptyMap();
        }
        if (!(value instanceof Map)) {
            throw new ObjException(op + " 的槽位应是对象, 实际是 " + typeOf(value));
        }
        return (Map<String, Object>) value;
    }

    static int asInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new ObjException("需要整数, 实际: " + value);
        }
    }

    static boolean truthy(Object value) {
        if (value == null || Boolean.FALSE.equals(value)) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0d;
        }
        if (value instanceof String) {
            String s = (String) value;
            return !s.isEmpty() && !"false".equals(s);
        }
        if (value instanceof java.util.Collection) {
            return !((java.util.Collection<?>) value).isEmpty();
        }
        if (value instanceof java.util.Map) {
            return !((java.util.Map<?, ?>) value).isEmpty();
        }
        return true;
    }

    /** 分组/透视/键值映射都按字符串归档键：H2 给 Long、JSON 给 Integer, 同值必须落到同一个桶 */
    static String key(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
        }
        return String.valueOf(value);
    }

    /** 表达式写成裸列名时, 产出的列名就用列名; 复杂表达式则整串作列名 */
    static String label(String expr) {
        String trimmed = expr.trim();
        return trimmed.matches("[A-Za-z_][A-Za-z0-9_]*") ? trimmed : expr;
    }

    static Object get(Object root, String path) {
        if (path == null || path.isEmpty()) {
            return root;
        }
        Object current = root;
        for (String seg : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(seg);
            } else if (current instanceof List) {
                List<?> list = (List<?>) current;
                int index = index(seg, list.size());
                if (index < 0) {
                    return null;
                }
                current = list.get(index);
            } else {
                return null;
            }
        }
        return current;
    }

    /** 沿路径逐层拷贝后写入, 返回新对象；路径上缺失的中间层自动补成空对象 */
    @SuppressWarnings("unchecked")
    static Map<String, Object> set(Map<String, Object> root, String path, Object value) {
        String[] segs = path.split("\\.");
        if (segs.length == 0) {
            throw new ObjException("path 不能为空");
        }
        return write(root, segs, 0, value);
    }

    private static Map<String, Object> write(Map<String, Object> node, String[] segs, int i, Object value) {
        Map<String, Object> copy = new LinkedHashMap<>(node);
        if (i == segs.length - 1) {
            copy.put(segs[i], value);
            return copy;
        }
        Object child = copy.get(segs[i]);
        Map<String, Object> childMap = child instanceof Map
                ? (Map<String, Object>) child : new LinkedHashMap<String, Object>();
        copy.put(segs[i], write(childMap, segs, i + 1, value));
        return copy;
    }

    static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 沿 path 写入由 container 算出的新值：当前值是数组就逐行写，是对象就直接写。
     * <p>
     * 回调用 container（持有 path 的那个对象）而不是路径上的旧值，所以
     * {@code {"op":"set","path":"total","expr":"price * qty"}} 读的就是本行的 price / qty。
     */
    static Object update(Object value, String path, Function<Map<String, Object>, Object> leaf) {
        if (value instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object item : (List<?>) value) {
                out.add(update(item, path, leaf));
            }
            return out;
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> container = (Map<String, Object>) value;
            return set(container, path, leaf.apply(container));
        }
        throw new ObjException("path 需要对象或行数组作为当前值, 实际是 " + typeOf(value));
    }

    static int index(String seg, int size) {
        try {
            int i = Integer.parseInt(seg);
            return i >= 0 && i < size ? i : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 宽松比较: 数字按数值, 同类型的其它值按自然序, 其余按字符串。
     * <p>
     * null 由调用方先处理 (聚合在取值阶段就滤掉, order 自己把空值排到最后);
     * 这里的 null 分支只是兜底, 规则是"null 比任何值都大"。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static int compare(Object a, Object b) {
        if (a == b) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        if (a.getClass() == b.getClass() && a instanceof Comparable) {
            return ((Comparable) a).compareTo(b);
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    static String typeOf(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }
}
