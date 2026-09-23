package com.zifang.util.db.query;

import com.zifang.util.db.support.Identifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 条件叶子：单列 + 算符 + 值。
 *
 * @author zifang
 */
public final class Criterion implements Predicate {

    private final String column;

    private final Op op;

    private final List<Object> values;

    Criterion(String column, Op op, List<Object> values) {
        this.column = Identifiers.require(column);
        this.op = op;
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        checkArity();
    }

    public String column() {
        return column;
    }

    public Op op() {
        return op;
    }

    public List<Object> values() {
        return values;
    }

    private void checkArity() {
        switch (op) {
            case IS_NULL:
            case IS_NOT_NULL:
                require(values.isEmpty(), op, 0);
                break;
            case BETWEEN:
                require(values.size() == 2, op, 2);
                break;
            case IN:
            case NOT_IN:
                if (values.isEmpty()) {
                    throw new IllegalArgumentException(op + " 至少需要一个值, 空集合请改用 Criteria.alwaysFalse()");
                }
                break;
            default:
                require(values.size() == 1, op, 1);
        }
    }

    private static void require(boolean ok, Op op, int expected) {
        if (!ok) {
            throw new IllegalArgumentException(op + " 需要 " + expected + " 个值, 实际不符");
        }
    }

    @Override
    public String toString() {
        return column + " " + op + " " + values;
    }
}
