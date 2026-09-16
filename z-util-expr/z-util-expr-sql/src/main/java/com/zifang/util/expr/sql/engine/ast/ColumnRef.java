package com.zifang.util.expr.sql.engine.ast;

/**
 * 列引用节点，如 a、t.a。
 */
public class ColumnRef implements Expression {

    private final String table;  // 可为 null
    private final String column;

    public ColumnRef(String column) {
        this(null, column);
    }

    public ColumnRef(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public String getTable() {
        return table;
    }

    public String getColumn() {
        return column;
    }

    /**
     * 获取不带表前缀的列名。
     */
    public String getUnqualifiedName() {
        return column;
    }

    /**
     * 获取完全限定名 table.column 或仅 column。
     */
    public String getQualifiedName() {
        return table != null ? table + "." + column : column;
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }
}
