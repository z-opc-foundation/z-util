package com.zifang.util.db.query;

/**
 * 恒真 / 恒假条件，用于空集合这类无解情形，避免生成非法 SQL。
 *
 * @author zifang
 */
final class ConstPredicate implements Predicate {

    static final Predicate TRUE = new ConstPredicate("1 = 1");

    static final Predicate FALSE = new ConstPredicate("1 = 0");

    private final String literal;

    private ConstPredicate(String literal) {
        this.literal = literal;
    }

    String literal() {
        return literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}
