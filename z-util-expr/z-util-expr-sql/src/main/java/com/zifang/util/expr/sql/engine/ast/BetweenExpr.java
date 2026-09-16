package com.zifang.util.expr.sql.engine.ast;

/**
 * BETWEEN 表达式节点，如 age BETWEEN 18 AND 65。
 */
public class BetweenExpr implements Expression {

    private final Expression expression;
    private final Expression low;
    private final Expression high;
    private final boolean negated;

    public BetweenExpr(Expression expression, Expression low, Expression high, boolean negated) {
        this.expression = expression;
        this.low = low;
        this.high = high;
        this.negated = negated;
    }

    public Expression getExpression() {
        return expression;
    }

    public Expression getLow() {
        return low;
    }

    public Expression getHigh() {
        return high;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression);
        if (negated) sb.append(" NOT");
        sb.append(" BETWEEN ").append(low).append(" AND ").append(high);
        return sb.toString();
    }
}
