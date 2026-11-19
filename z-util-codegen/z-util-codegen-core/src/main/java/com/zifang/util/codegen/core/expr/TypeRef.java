package com.zifang.util.codegen.core.expr;

/**
 * 类型引用：注入成员时声明类型的方式。
 *
 * <p>刻意不暴露 javac AST——handler 只描述"要什么类型"，由注入器决定如何落到 AST 上。</p>
 *
 * @author zifang
 */
public sealed interface TypeRef {

    /** 取目标类中某个字段的声明类型（复用其类型树，泛型/数组/通配符都能原样带过来）。 */
    record OfField(String fieldName) implements TypeRef {
    }

    /** 全限定名，如 {@code java.util.List}、{@code java.util.Map.Entry}。 */
    record Qualified(String qualifiedName) implements TypeRef {
    }

    /** 基本类型关键字，如 {@code void}、{@code int}、{@code boolean}。 */
    record Primitive(String keyword) implements TypeRef {
    }

    /** 类型变量名，如 {@code T}（在目标类的类型参数作用域内解析）。 */
    record Variable(String name) implements TypeRef {
    }

    static TypeRef ofField(String fieldName) {
        return new OfField(fieldName);
    }

    static TypeRef of(String qualifiedName) {
        return new Qualified(qualifiedName);
    }

    static TypeRef primitive(String keyword) {
        return new Primitive(keyword);
    }

    static TypeRef variable(String name) {
        return new Variable(name);
    }
}
