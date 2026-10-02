package com.zifang.util.expression.instruction;

public enum Operator {
    ADD("加法", "+"),
    SUBTRACT("减法", "-"),
    MULTIPLY("乘法", "*"),
    DIVIDE("除法", "/");

    private final String name;
    private final String value;

    Operator(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}