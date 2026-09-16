package com.zifang.util.expr.sql.engine.ast;

/**
 * CAST 表达式节点，如 CAST(x AS INTEGER)。
 */
public class CastExpr implements Expression {

    private final Expression expression;
    private final String targetType;

    public CastExpr(Expression expression, String targetType) {
        this.expression = expression;
        this.targetType = targetType;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getTargetType() {
        return targetType;
    }

    @Override
    public String toString() {
        return "CAST(" + expression + " AS " + targetType + ")";
    }
}
