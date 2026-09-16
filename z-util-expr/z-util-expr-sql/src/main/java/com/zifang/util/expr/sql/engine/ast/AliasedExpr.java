package com.zifang.util.expr.sql.engine.ast;

/**
 * 带别名的表达式节点，如 price * qty AS total。
 */
public class AliasedExpr implements Expression {

    private final Expression expression;
    private final String alias;

    public AliasedExpr(Expression expression, String alias) {
        this.expression = expression;
        this.alias = alias;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getAlias() {
        return alias;
    }

    /**
     * 获取输出列名：优先使用别名，否则尝试从列引用中提取。
     */
    public String getOutputName() {
        if (alias != null) return alias;
        if (expression instanceof ColumnRef) return ((ColumnRef) expression).getColumn();
        return expression.toString();
    }

    @Override
    public String toString() {
        if (alias != null) return expression + " AS " + alias;
        return expression.toString();
    }
}
