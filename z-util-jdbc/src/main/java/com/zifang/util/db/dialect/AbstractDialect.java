package com.zifang.util.db.dialect;

import com.zifang.util.db.support.Identifiers;

/**
 * 方言基类：标识符引用、分页与计数子句的公共骨架。
 * <p>
 * 三库都支持标准的 {@code LIMIT n OFFSET o}，故分页写法无需逐方言分叉；
 * 差异集中在驱动、URL 与引用符上。
 *
 * @author zifang
 */
public abstract class AbstractDialect implements Dialect {

    private final String id;
    private final String driverClassName;
    private final char quoteChar;

    protected AbstractDialect(String id, String driverClassName, char quoteChar) {
        this.id = id;
        this.driverClassName = driverClassName;
        this.quoteChar = quoteChar;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String driverClassName() {
        return driverClassName;
    }

    @Override
    public String quote(String qualifiedName) {
        String[] segments = Identifiers.parts(qualifiedName);
        StringBuilder sb = new StringBuilder(qualifiedName.length() + segments.length * 2);
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(quoteChar).append(segments[i]).append(quoteChar);
        }
        return sb.toString();
    }

    @Override
    public String limitClause(long offset, long limit) {
        if (limit < 0) {
            return "";
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset 不能为负: " + offset);
        }
        StringBuilder sb = new StringBuilder(" LIMIT ").append(limit);
        if (offset > 0) {
            sb.append(" OFFSET ").append(offset);
        }
        return sb.toString();
    }

    @Override
    public String countSql(String selectSql) {
        return "SELECT COUNT(*) FROM (" + selectSql + ") z_cnt";
    }

    @Override
    public String validationQuery() {
        return "SELECT 1";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + id + ")";
    }
}
