package com.zifang.util.core.pattern.visitor;

/**
 * GoF Visitor: 在不改动元素类层次的前提下给类层次添加新操作.
 * <p>
 * 双分派: object.accept(this) 把自身类型分发回 visit, 解决 method overload 不区分运行时类型的问题.
 */
public interface Visitor<R> {

    R visit(Visitable element);
}