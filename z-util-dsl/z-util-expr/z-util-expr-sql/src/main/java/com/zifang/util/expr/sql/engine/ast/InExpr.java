package com.zifang.util.expr.sql.engine.ast;

import java.util.List;

/**
 * IN 表达式节点，如 status IN (1, 2, 3)；
 * values 为 null 时表示 IN (SELECT …) 子查询形态（{@link #getSubquery()} 非 null）。
 */
public class InExpr implements Expression {

    private final Expression expression;
    private final List<Expression> values;
    private final SelectStmt subquery;
    private final boolean negated;

    public InExpr(Expression expression, List<Expression> values, boolean negated) {
        this(expression, values, null, negated);
    }

    public InExpr(Expression expression, SelectStmt subquery, boolean negated) {
        this(expression, null, subquery, negated);
    }

    private InExpr(Expression expression, List<Expression> values, SelectStmt subquery, boolean negated) {
        this.expression = expression;
        this.values = values;
        this.subquery = subquery;
        this.negated = negated;
    }

    public Expression getExpression() {
        return expression;
    }

    public List<Expression> getValues() {
        return values;
    }

    public SelectStmt getSubquery() {
        return subquery;
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
        if (subquery != null) {
            sb.append(subquery);
        } else {
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(values.get(i));
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
