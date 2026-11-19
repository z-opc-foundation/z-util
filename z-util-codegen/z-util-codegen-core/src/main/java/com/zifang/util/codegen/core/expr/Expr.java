package com.zifang.util.codegen.core.expr;

import java.util.List;

/**
 * 表达式 DSL：注入成员的方法体用这套结构描述，注入器负责映射成 javac AST。
 *
 * @author zifang
 */
public sealed interface Expr {

    /** {@code this}。 */
    record ThisRef() implements Expr {
    }

    /** {@code super}。 */
    record SuperRef() implements Expr {
    }

    /** 字段访问 {@code target.name}。 */
    record Field(Expr target, String name) implements Expr {
    }

    /** 标识符引用（形参、局部变量）。 */
    record Var(String name) implements Expr {
    }

    /** 字面量：String / Integer / Long / Double / Boolean / Character。 */
    record Literal(Object value) implements Expr {
    }

    /** {@code null}。 */
    record NullLiteral() implements Expr {
    }

    /** 方法调用 {@code target.method(args)}。 */
    record Call(Expr target, String method, List<Expr> arguments) implements Expr {
    }

    /** 类型名（作为静态调用接收者），如 {@code java.util.Objects}。 */
    record TypeName(String qualifiedName) implements Expr {
    }

    /** 二元运算，运算符取 {@code == != < > <= >= && || + - * / %}。 */
    record Binary(String operator, Expr left, Expr right) implements Expr {
    }

    /** 一元运算 {@code -x} / {@code !x}。 */
    record Unary(String operator, Expr operand) implements Expr {
    }

    /** 类型转换 {@code (Type) value}。 */
    record Cast(TypeRef type, Expr value) implements Expr {
    }

    /** {@code value instanceof Type}。 */
    record InstanceOf(Expr value, TypeRef type) implements Expr {
    }

    /** {@code new Type(args)}。 */
    record New(TypeRef type, List<Expr> arguments) implements Expr {
    }

    /** 类字面量 {@code Type.class}。 */
    record ClassLiteral(TypeRef type) implements Expr {
    }

    /** 数组字面量 {@code new T[]{a, b}}。 */
    record ArrayOf(TypeRef componentType, List<Expr> elements) implements Expr {
    }

    /** 三元 {@code cond ? a : b}。 */
    record Conditional(Expr condition, Expr whenTrue, Expr whenFalse) implements Expr {
    }

    static Expr field(Expr target, String name) {
        return new Field(target, name);
    }

    static Expr thisRef() {
        return new ThisRef();
    }

    static Expr superRef() {
        return new SuperRef();
    }

    static Expr var(String name) {
        return new Var(name);
    }

    static Expr literal(Object value) {
        return new Literal(value);
    }

    static Expr nullLiteral() {
        return new NullLiteral();
    }

    static Expr call(Expr target, String method, Expr... arguments) {
        return new Call(target, method, List.of(arguments));
    }

    static Expr typeName(String qualifiedName) {
        return new TypeName(qualifiedName);
    }

    static Expr binary(String operator, Expr left, Expr right) {
        return new Binary(operator, left, right);
    }

    static Expr unary(String operator, Expr operand) {
        return new Unary(operator, operand);
    }

    static Expr cast(TypeRef type, Expr value) {
        return new Cast(type, value);
    }

    static Expr instanceOf(Expr value, TypeRef type) {
        return new InstanceOf(value, type);
    }

    static Expr create(TypeRef type, Expr... arguments) {
        return new New(type, List.of(arguments));
    }

    static Expr classLiteral(TypeRef type) {
        return new ClassLiteral(type);
    }

    static Expr arrayOf(TypeRef componentType, List<Expr> elements) {
        return new ArrayOf(componentType, elements);
    }

    static Expr conditional(Expr condition, Expr whenTrue, Expr whenFalse) {
        return new Conditional(condition, whenTrue, whenFalse);
    }
}
