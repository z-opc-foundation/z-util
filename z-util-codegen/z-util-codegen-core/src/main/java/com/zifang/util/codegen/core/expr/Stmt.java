package com.zifang.util.codegen.core.expr;

import java.util.List;

/**
 * 语句 DSL：与 {@link Expr} 同构，注入器负责映射成 javac AST。
 *
 * @author zifang
 */
public sealed interface Stmt {

    /** {@code return expr;}。 */
    record Return(Expr value) implements Stmt {
    }

    /** 表达式语句 {@code expr;}。 */
    record ExprStmt(Expr expression) implements Stmt {
    }

    /** 局部变量声明 {@code Type name = init;}。 */
    record VarDecl(TypeRef type, String name, Expr initializer) implements Stmt {
    }

    /** 赋值 {@code target = value;}。 */
    record Assign(Expr target, Expr value) implements Stmt {
    }

    /** {@code throw value;}。 */
    record Throw(Expr value) implements Stmt {
    }

    /** {@code if (cond) then else otherwise}。 */
    record If(Expr condition, Stmt then, Stmt otherwise) implements Stmt {
    }

    /** 语句块。 */
    record Block(List<Stmt> body) implements Stmt {
    }

    static Stmt ret(Expr value) {
        return new Return(value);
    }

    static Stmt expr(Expr expression) {
        return new ExprStmt(expression);
    }

    static Stmt var(TypeRef type, String name, Expr initializer) {
        return new VarDecl(type, name, initializer);
    }

    static Stmt assign(Expr target, Expr value) {
        return new Assign(target, value);
    }

    static Stmt thrown(Expr value) {
        return new Throw(value);
    }

    static Stmt ifThen(Expr condition, Stmt then) {
        return new If(condition, then, null);
    }

    static Stmt ifElse(Expr condition, Stmt then, Stmt otherwise) {
        return new If(condition, then, otherwise);
    }

    static Stmt block(List<Stmt> body) {
        return new Block(body);
    }
}
