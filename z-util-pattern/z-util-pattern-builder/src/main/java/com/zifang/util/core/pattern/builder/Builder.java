package com.zifang.util.core.pattern.builder;

/**
 * GoF Builder: 链式 setXxx().build() 构造复杂对象.
 * <p>
 * 与 {@link com.zifang.util.core.pattern.template.Template} 的区别: Builder 控制的是构造流程,
 * Template 控制的是算法骨架.
 */
public abstract class Builder<T> {

    public abstract Builder<T> reset();

    protected abstract T build();

    /** 子类在 build 前必须调 validate. */
    protected void validate() {}

    /** 链式结束: 调 validate + build. */
    public final T assemble() {
        validate();
        return build();
    }
}