package com.zifang.util.expr.sql.engine.ast;

import java.util.List;

/**
 * IN 表达式节点，如 status IN (1, 2, 3)。
 */
public class InExpr implements Expression {

    private final Expression expression;
    private final List<Expression> values;
    private final boolean negated;

    public InExpr(Expression expression, List<Expression> values, boolean negated) {
        this.expression = expression;
        this.values = values;
        this.negated = negated;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Expression> getValues() {
        return values;
    }

    public boolean isNegated() {
        return negated;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression);
        if (negated) sb.append(" NOT");
        sb.append(" IN (");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(values.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}
