package com.zifang.util.expr.sql.engine.ast;

import java.util.Collections;
import java.util.List;

/**
 * 函数调用表达式节点，如 ABS(x)、COUNT(*)。
 */
public class FunctionCall implements Expression {

    private final String name;
    private final List<Expression> arguments;
    private final boolean distinct;

    public FunctionCall(String name, List<Expression> arguments) {
        this(name, arguments, false);
    }

    public FunctionCall(String name, List<Expression> arguments, boolean distinct) {
        this.name = name;
        this.arguments = arguments != null ? arguments : Collections.emptyList();
        this.distinct = distinct;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        if (distinct) sb.append("DISTINCT ");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}
