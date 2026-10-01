package com.zifang.util.core.pattern.visitor;

/**
 * 被访问的元素标记. 实现 accept 把自身回传给 visitor.visit,
 * 由 visitor 通过 instanceof 区分运行时类型.
 * <p>
 * 注: 早期版本写成 {@code <T extends Visitable<T>>} 的 F-bounded 多态形式, javac
 * 处理 cross-package forward ref 时偶发 cannot-find-symbol, 退化成裸标记接口
 * 由 visitor 自己 instanceof 即可, 业务收益等价.
 */
public interface Visitable {

    <R> R accept(Visitor<R> visitor);
}