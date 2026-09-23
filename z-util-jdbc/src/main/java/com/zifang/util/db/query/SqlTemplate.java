package com.zifang.util.db.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 命名参数 SQL 模板：{@code ${name}} 占位，编译为带 {@code ?} 的 {@link SqlSpec}。
 * <p>
 * 面向 BI 数据集这类"SQL 由使用者书写"的场景：模板文本本身不可参数化，
 * 因此只接受结构完整的 SELECT，占位一律转成绑定参数，绝不做字符串直插。
 * 需要动态表名/列名/排序方向时请改用 {@link Query}。
 * <p>
 * 字符串字面量与注释内的 {@code ${...}} 不会被当作占位。
 *
 * @author zifang
 */
public final class SqlTemplate {

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,64}");

    private final String raw;

    private final List<String> placeholders;

    private final int markerCount;

    private SqlTemplate(String raw, List<String> placeholders, int markerCount) {
        this.raw = raw;
        this.placeholders = Collections.unmodifiableList(placeholders);
        this.markerCount = markerCount;
    }

    public static SqlTemplate of(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL 模板不能为空");
        }
        return parse(sql);
    }

    public String raw() {
        return raw;
    }

    /**
     * 占位名，按出现顺序。
     */
    public List<String> placeholders() {
        return placeholders;
    }

    /**
     * 模板中裸 {@code ?} 的个数；不为 0 说明混用了两种参数写法。
     */
    public int unboundMarkerCount() {
        return markerCount;
    }

    public SqlSpec bind(Map<String, ?> params) {
        if (markerCount > 0) {
            throw new IllegalArgumentException("模板中出现了裸 ? 占位, 请统一使用 ${name}: " + raw);
        }
        Map<String, ?> values = params == null ? Collections.<String, Object>emptyMap() : params;
        List<Object> bound = new ArrayList<>(placeholders.size());
        for (String name : placeholders) {
            if (!values.containsKey(name)) {
                throw new IllegalArgumentException("缺少参数: " + name);
            }
            bound.add(values.get(name));
        }
        return new SqlSpec(toParameterized(), bound);
    }

    /**
     * 把 {@code ${name}} 替换为 {@code ?}。
     */
    public String toParameterized() {
        StringBuilder sb = new StringBuilder(raw.length());
        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (c == '\'' || c == '"' || c == '-' || c == '/') {
                int end = literalEnd(raw, i);
                sb.append(raw, i, end);
                i = end;
                continue;
            }
            if (c == '$' && i + 1 < raw.length() && raw.charAt(i + 1) == '{') {
                int close = raw.indexOf('}', i);
                if (close > 0) {
                    sb.append('?');
                    i = close + 1;
                    continue;
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private static SqlTemplate parse(String sql) {
        List<String> names = new ArrayList<>();
        int markers = 0;
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == '\'' || c == '"' || c == '-' || c == '/') {
                i = literalEnd(sql, i);
                continue;
            }
            if (c == '?') {
                markers++;
                i++;
                continue;
            }
            if (c == '$' && i + 1 < sql.length() && sql.charAt(i + 1) == '{') {
                int close = sql.indexOf('}', i);
                if (close < 0) {
                    throw new IllegalArgumentException("模板 ${ 未闭合: " + sql);
                }
                String name = sql.substring(i + 2, close).trim();
                if (!NAME.matcher(name).matches()) {
                    throw new IllegalArgumentException("非法参数名 ${" + name + "}");
                }
                names.add(name);
                i = close + 1;
                continue;
            }
            i++;
        }
        return new SqlTemplate(sql, names, markers);
    }

    /**
     * 返回从 from 起的字符串字面量 / 注释的结束下标；非上述起点时返回 from 本身。
     */
    private static int literalEnd(String sql, int from) {
        char c = sql.charAt(from);
        if (c == '\'') {
            int i = from + 1;
            while (i < sql.length()) {
                if (sql.charAt(i) == '\'') {
                    if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                        i += 2;
                        continue;
                    }
                    return i + 1;
                }
                i++;
            }
            return sql.length();
        }
        if (c == '"') {
            int close = sql.indexOf('"', from + 1);
            return close < 0 ? sql.length() : close + 1;
        }
        if (c == '-' && from + 1 < sql.length() && sql.charAt(from + 1) == '-') {
            int newline = sql.indexOf('\n', from);
            return newline < 0 ? sql.length() : newline;
        }
        if (c == '/' && from + 1 < sql.length() && sql.charAt(from + 1) == '*') {
            int close = sql.indexOf("*/", from + 2);
            return close < 0 ? sql.length() : close + 2;
        }
        return from + 1;
    }

    @Override
    public String toString() {
        return "SqlTemplate{" + raw + ", params=" + placeholders + "}";
    }
}
