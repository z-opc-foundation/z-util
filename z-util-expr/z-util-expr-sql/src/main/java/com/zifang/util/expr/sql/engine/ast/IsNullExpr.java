package com.zifang.util.expr.sql.engine.ast;

/**
 * IS NULL / IS NOT NULL 表达式节点。
 */
public class IsNullExpr implements Expression {

    private final Expression expression;
    private final boolean negated;

    public IsNullExpr(Expression expression, boolean negated) {
        this.expression = expression;
        this.negated = negated;
    }

    public Expression getExpression() {
        return expression;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public String toString() {
        return expression + " IS " + (negated ? "NOT " : "") + "NULL";
    }
}
