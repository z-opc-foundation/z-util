package com.zifang.util.core.pattern.template;

/**
 * GoF Template Method: 抽象类定骨架, 子类填步骤.
 * <p>
 * 子类覆写 step1/step2 (final template() 不可改). 比 strategy 更适合 "流程固定, 实现可换".
 */
public abstract class Template<P, R> {

    /** 模板入口, 不允许覆写. */
    public final R execute(P param) {
        before(param);
        R r = doStep(param);
        after(param, r);
        return r;
    }

    protected void before(P param) {}

    /** 子类必须实现的主步骤. */
    protected abstract R doStep(P param);

    protected void after(P param, R result) {}
}