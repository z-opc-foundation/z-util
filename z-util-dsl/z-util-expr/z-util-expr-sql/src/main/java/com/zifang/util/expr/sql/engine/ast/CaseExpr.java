package com.zifang.util.expr.sql.engine.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CASE 表达式节点，支持两种 MySQL 形态：
 * <ul>
 *   <li>简单 CASE：{@code CASE dept WHEN 'A' THEN 1 WHEN 'B' THEN 2 ELSE 0 END}</li>
 *   <li>搜索 CASE：{@code CASE WHEN amount >= 400 THEN 'BIG' ELSE 'SMALL' END}</li>
 * </ul>
 * {@code operand} 为 null 表示搜索 CASE；此时 conditions 为布尔条件，
 * 否则 conditions 为与 operand 逐一比对的候选值。
 */
public class CaseExpr implements Expression {

    private final Expression operand;
    private final List<Expression> conditions;
    private final List<Expression> results;
    private final Expression elseResult;

    public CaseExpr(Expression operand, List<Expression> conditions, List<Expression> results, Expression elseResult) {
        this.operand = operand;
        this.conditions = conditions != null ? conditions : Collections.<Expression>emptyList();
        this.results = results != null ? results : Collections.<Expression>emptyList();
        this.elseResult = elseResult;
    }

    public Expression getOperand() {
        return operand;
    }

    public List<Expression> getConditions() {
        return conditions;
    }

    public List<Expression> getResults() {
        return results;
    }

    public Expression getElseResult() {
        return elseResult;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CASE");
        if (operand != null) {
            sb.append(' ').append(operand);
        }
        for (int i = 0; i < conditions.size(); i++) {
            sb.append(" WHEN ").append(conditions.get(i)).append(" THEN ").append(results.get(i));
        }
        if (elseResult != null) {
            sb.append(" ELSE ").append(elseResult);
        }
        return sb.append(" END").toString();
    }
}
