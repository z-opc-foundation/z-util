package com.zifang.util.db.query;

/**
 * 条件算符。行级比较，不含聚合与函数。
 *
 * @author zifang
 */
public enum Op {

    EQ("="),
    NE("<>"),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    IN(null),
    NOT_IN(null),
    BETWEEN(null),
    IS_NULL(null),
    IS_NOT_NULL(null);

    private final String symbol;

    Op(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 二元比较的 SQL 运算符；集合/区间/空值类算符返回 null，由编译器特殊处理。
     */
    public String symbol() {
        return symbol;
    }
}
