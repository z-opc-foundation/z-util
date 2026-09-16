package com.zifang.util.expr.sql.engine.ast;

/**
 * ORDER BY 排序键。
 */
public class SortKey {

    private final Expression expression;
    private final boolean descending;

    public SortKey(Expression expression, boolean descending) {
        this.expression = expression;
        this.descending = descending;
    }

    public Expression getExpression() {
        return expression;
    }

    public boolean isDescending() {
        return descending;
    }

    @Override
    public String toString() {
        return expression + (descending ? " DESC" : " ASC");
    }
}
