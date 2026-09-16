package com.zifang.util.expr.sql.engine.ast;

import java.math.BigDecimal;

/**
 * 字面量节点（数字、字符串、NULL、布尔）。
 */
public class Literal implements Expression {

    private final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public static Literal ofString(String text) {
        return new Literal(text);
    }

    public static Literal ofNumber(String text) {
        if (text.contains(".") || text.contains("e") || text.contains("E")) {
            return new Literal(Double.parseDouble(text));
        }
        try {
            return new Literal(Long.parseLong(text));
        } catch (NumberFormatException e) {
            return new Literal(new BigDecimal(text));
        }
    }

    public static Literal ofNull() {
        return new Literal(null);
    }

    public static Literal ofBoolean(boolean value) {
        return new Literal(value);
    }

    @Override
    public String toString() {
        if (value == null) return "NULL";
        if (value instanceof String) return "'" + value + "'";
        return value.toString();
    }
}
