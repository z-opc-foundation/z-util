package com.zifang.util.expr.obj;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 聚合函数：{@code group.agg} / {@code fold.agg} / {@code pivot} 的取值规则。
 * <p>
 * 写法固定为 {@code SUM(amount)} 这种函数调用形态，括号内交给 EL 求值，
 * 所以 {@code SUM(amount * 1.13)}、{@code COUNT(DISTINCT city)} 都是同一个解析路径。
 *
 * @author zifang
 */
final class Aggregates {

    private static final Pattern CALL = Pattern.compile("^\\s*([A-Za-z_]+)\\s*\\((.*)\\)\\s*$", Pattern.DOTALL);

    private static final Set<String> SUPPORTED = new LinkedHashSet<>(
            java.util.Arrays.asList("SUM", "COUNT", "AVG", "MIN", "MAX", "FIRST", "LAST", "COLLECT"));

    private Aggregates() {
    }

    static Object evaluate(String aggExpr, List<Map<String, Object>> rows, BiFunction<String, Map<String, Object>, Object> el) {
        Matcher m = CALL.matcher(aggExpr);
        if (!m.matches()) {
            throw new ObjException("聚合表达式要写成函数调用形态, 例如 SUM(amount) / COUNT(*), 实际: " + aggExpr);
        }
        String name = m.group(1).toUpperCase(java.util.Locale.ROOT);
        if (!SUPPORTED.contains(name)) {
            throw new ObjException("不支持的聚合函数 " + name + "(...), 可用: " + SUPPORTED);
        }
        String inner = m.group(2).trim();
        boolean distinct = inner.regionMatches(true, 0, "DISTINCT ", 0, 9);
        if (distinct) {
            inner = inner.substring(9).trim();
        }

        if ("COUNT".equals(name) && "*".equals(inner)) {
            return (long) rows.size();
        }
        List<Object> values = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Object v = "*".equals(inner) ? row : el.apply(inner, row);
            if (v != null) {
                values.add(v);
            }
        }
        if (distinct) {
            Set<String> seen = new LinkedHashSet<>();
            List<Object> unique = new ArrayList<>();
            for (Object v : values) {
                if (seen.add(Values.key(v))) {
                    unique.add(v);
                }
            }
            values = unique;
        }
        return apply(name, values);
    }

    private static Object apply(String name, List<Object> values) {
        switch (name) {
            case "COUNT":
                return (long) values.size();
            case "COLLECT":
                return new ArrayList<>(values);
            case "FIRST":
                return values.isEmpty() ? null : values.get(0);
            case "LAST":
                return values.isEmpty() ? null : values.get(values.size() - 1);
            case "MIN": {
                Object min = null;
                for (Object v : values) {
                    if (min == null || Values.compare(v, min) < 0) {
                        min = v;
                    }
                }
                return min;
            }
            case "MAX": {
                Object max = null;
                for (Object v : values) {
                    if (max == null || Values.compare(v, max) > 0) {
                        max = v;
                    }
                }
                return max;
            }
            case "SUM": {
                double sum = 0d;
                boolean integral = true;
                for (Object v : values) {
                    sum += number(v);
                    integral &= isIntegral(v);
                }
                return integral ? (Object) (long) sum : (Object) sum;
            }
            case "AVG": {
                if (values.isEmpty()) {
                    return null;
                }
                double sum = 0d;
                for (Object v : values) {
                    sum += number(v);
                }
                return sum / values.size();
            }
            default:
                throw new ObjException("不支持的聚合函数: " + name);
        }
    }

    private static double number(Object v) {
        if (!(v instanceof Number)) {
            throw new ObjException("聚合只接受数值列, 实际拿到 " + Values.typeOf(v) + ": " + v);
        }
        return ((Number) v).doubleValue();
    }

    /** 全整型列给 Long, 掺了浮点才给 Double: 前端拿到的 1000 不该渲染成 1000.0 */
    private static boolean isIntegral(Object v) {
        if (v instanceof Integer || v instanceof Long || v instanceof Short || v instanceof Byte) {
            return true;
        }
        if (v instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) v).scale() <= 0;
        }
        double d = ((Number) v).doubleValue();
        return d == Math.floor(d) && !Double.isInfinite(d);
    }
}
