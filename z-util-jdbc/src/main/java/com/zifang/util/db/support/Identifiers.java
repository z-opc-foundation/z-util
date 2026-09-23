package com.zifang.util.db.support;

import java.util.regex.Pattern;

/**
 * SQL 标识符（表名 / 列名）白名单校验与拆解。
 * <p>
 * 标识符无法走 JDBC 参数绑定、只能拼进 SQL 文本，所以必须先校验再引用，
 * 否则条件对象会把注入面从值域挪到名字域。规则与引用符无关，取三库交集。
 *
 * @author zifang
 */
public final class Identifiers {

    private static final Pattern SEGMENT = Pattern.compile("[A-Za-z0-9_]{1,128}");

    private Identifiers() {
    }

    public static boolean isSafe(String identifier) {
        return identifier != null && SEGMENT.matcher(identifier).matches();
    }

    /**
     * 校验单个标识符，非法时抛 {@link IllegalArgumentException}。
     */
    public static String require(String identifier) {
        if (!isSafe(identifier)) {
            throw new IllegalArgumentException("非法 SQL 标识符: " + identifier);
        }
        return identifier;
    }

    /**
     * 校验 {@code table} 或 {@code schema.table}，返回拆出的各段（未加引用符）。
     */
    public static String[] parts(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            throw new IllegalArgumentException("标识符不能为空");
        }
        String[] raw = qualifiedName.split("\\.", -1);
        if (raw.length > 2) {
            throw new IllegalArgumentException("标识符至多两段 (table 或 schema.table): " + qualifiedName);
        }
        for (String seg : raw) {
            require(seg);
        }
        return raw;
    }

    public static boolean isSafeQualifiedName(String qualifiedName) {
        try {
            parts(qualifiedName);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
