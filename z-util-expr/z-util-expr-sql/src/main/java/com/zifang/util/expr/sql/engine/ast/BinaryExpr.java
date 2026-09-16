package com.zifang.util.expr.sql.engine.ast;

/**
 * 二元运算表达式节点。
 * 支持算术（+ - * / %）、比较（= <> > < >= <= LIKE）、逻辑（AND OR）。
 */
public class BinaryExpr implements Expression {

    private final Expression left;
    private final String operator;
    private final Expression right;

    public BinaryExpr(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}
