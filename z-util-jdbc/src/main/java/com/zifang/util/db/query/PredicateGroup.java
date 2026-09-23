package com.zifang.util.db.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 条件分组：以 AND / OR 连接若干子节点，可嵌套。
 *
 * @author zifang
 */
public final class PredicateGroup implements Predicate {

    private final Logic logic;

    private final List<Predicate> children;

    PredicateGroup(Logic logic, List<Predicate> children) {
        this.logic = logic;
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
    }

    public Logic logic() {
        return logic;
    }

    public List<Predicate> children() {
        return children;
    }

    @Override
    public String toString() {
        return logic.name() + children;
    }
}
