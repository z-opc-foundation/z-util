package com.zifang.util.db.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 条件工厂，动态查询的唯一入口。
 * <pre>
 *   Query.select("id", "amount").from("t_order")
 *        .where(Criteria.ge("amount", 100))
 *        .and(Criteria.or(Criteria.eq("status", "PAID"), Criteria.in("channel", "app", "web")))
 *        .orderBy("created_at", false)
 *        .limit(20);
 * </pre>
 * 值一律成为绑定参数，列名走标识符白名单后由方言引用。
 *
 * @author zifang
 */
public final class Criteria {

    private Criteria() {
    }

    public static Predicate eq(String column, Object value) {
        return new Criterion(column, Op.EQ, one(value));
    }

    public static Predicate ne(String column, Object value) {
        return new Criterion(column, Op.NE, one(value));
    }

    public static Predicate gt(String column, Object value) {
        return new Criterion(column, Op.GT, one(value));
    }

    public static Predicate ge(String column, Object value) {
        return new Criterion(column, Op.GE, one(value));
    }

    public static Predicate lt(String column, Object value) {
        return new Criterion(column, Op.LT, one(value));
    }

    public static Predicate le(String column, Object value) {
        return new Criterion(column, Op.LE, one(value));
    }

    /**
     * 通配符由调用方给出（如 {@code "%张%"}），此处不猜语义。
     */
    public static Predicate like(String column, Object pattern) {
        return new Criterion(column, Op.LIKE, one(pattern));
    }

    public static Predicate notLike(String column, Object pattern) {
        return new Criterion(column, Op.NOT_LIKE, one(pattern));
    }

    public static Predicate contains(String column, Object fragment) {
        return like(column, "%" + fragment + "%");
    }

    public static Predicate in(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return alwaysFalse();
        }
        return new Criterion(column, Op.IN, new ArrayList<Object>(values));
    }

    public static Predicate in(String column, Object... values) {
        return in(column, values == null ? Collections.emptyList() : Arrays.asList(values));
    }

    public static Predicate notIn(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return alwaysTrue();
        }
        return new Criterion(column, Op.NOT_IN, new ArrayList<Object>(values));
    }

    public static Predicate notIn(String column, Object... values) {
        return notIn(column, values == null ? Collections.emptyList() : Arrays.asList(values));
    }

    public static Predicate between(String column, Object from, Object to) {
        return new Criterion(column, Op.BETWEEN, Arrays.asList(from, to));
    }

    public static Predicate isNull(String column) {
        return new Criterion(column, Op.IS_NULL, Collections.emptyList());
    }

    public static Predicate isNotNull(String column) {
        return new Criterion(column, Op.IS_NOT_NULL, Collections.emptyList());
    }

    public static Predicate and(Predicate... children) {
        return group(Logic.AND, children);
    }

    public static Predicate or(Predicate... children) {
        return group(Logic.OR, children);
    }

    public static Predicate alwaysTrue() {
        return ConstPredicate.TRUE;
    }

    public static Predicate alwaysFalse() {
        return ConstPredicate.FALSE;
    }

    private static Predicate group(Logic logic, Predicate... children) {
        if (children == null || children.length == 0) {
            return logic == Logic.AND ? alwaysTrue() : alwaysFalse();
        }
        return new PredicateGroup(logic, Arrays.asList(children));
    }

    private static List<Object> one(Object value) {
        List<Object> list = new ArrayList<>(1);
        list.add(value);
        return list;
    }
}
