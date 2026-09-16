package com.zifang.util.expr.sql.engine.ast;

/**
 * 一元运算表达式节点（NOT、取负）。
 */
public class UnaryExpr implements Expression {

    private final String operator;
    private final Expression operand;

    public UnaryExpr(String operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return operator + " " + operand;
    }
}
