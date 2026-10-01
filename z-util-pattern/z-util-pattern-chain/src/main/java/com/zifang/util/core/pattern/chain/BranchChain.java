package com.zifang.util.core.pattern.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 分支链实现
 * <p>
 * 根据条件选择不同的子链执行
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class BranchChain<C extends ChainContext<?, ?>> implements Chain<C> {

    private final Predicate<C> condition;
    private final String name;
    private Chain<C> trueChain;
    private Chain<C> falseChain;

    /**
     * BranchChain方法。
     * * @param condition PredicateC类型参数
     */
    public BranchChain(Predicate<C> condition) {
        this(condition, "BranchChain-" + System.currentTimeMillis());
    }

    /**
     * BranchChain方法。
     * * @param condition PredicateC类型参数
     *
     * @param name String类型参数
     */
    public BranchChain(Predicate<C> condition, String name) {
        this.condition = condition;
        this.trueChain = Chain.empty();
        this.falseChain = Chain.empty();
        this.name = name;
    }

    /**
     * 设置条件为true时执行的链
     */
    public BranchChain<C> whenTrue(Chain<C> chain) {
        this.trueChain = chain;
        return this;
    }

    /**
     * 设置条件为true时执行的处理器
     */
    public BranchChain<C> whenTrue(Processor<C> processor) {
        this.trueChain = Chain.of(processor);
        return this;
    }

    /**
     * 设置条件为false时执行的链
     */
    public BranchChain<C> whenFalse(Chain<C> chain) {
        this.falseChain = chain;
        return this;
    }

    /**
     * 设置条件为false时执行的处理器
     */
    public BranchChain<C> whenFalse(Processor<C> processor) {
        this.falseChain = Chain.of(processor);
        return this;
    }

    /**
     * 快捷方法：if-else结构
     */
    public BranchChain<C> branch(Chain<C> trueBranch, Chain<C> falseBranch) {
        this.trueChain = trueBranch;
        this.falseChain = falseBranch;
        return this;
    }

    @Override
    /**
     * getName方法。
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    @Override
    /**
     * getProcessors方法。
     * @return List<Processor<C>>类型返回值
     */
    public List<Processor<C>> getProcessors() {
        List<Processor<C>> result = new ArrayList<>();
        result.add(context -> {
            if (condition.test(context)) {
                return trueChain.process(context);
            } else {
                return falseChain.process(context);
            }
        });
        return result;
    }

    @Override
    /**
     * process方法。
     *      * @param context C类型参数
     * @return ProcessorResult类型返回值
     */
    public ProcessorResult process(C context) {
        if (condition.test(context)) {
            return trueChain.process(context);
        } else {
            return falseChain.process(context);
        }
    }

    @Override
    /**
     * addProcessor方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addProcessor(Processor<C> processor) {
        trueChain.addProcessor(processor);
        return this;
    }

    @Override
    /**
     * addFirst方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addFirst(Processor<C> processor) {
        trueChain.addFirst(processor);
        return this;
    }

    @Override
    /**
     * addAt方法。
     *      * @param index int类型参数
     * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addAt(int index, Processor<C> processor) {
        trueChain.addAt(index, processor);
        return this;
    }

    @Override
    /**
     * remove方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> remove(Processor<C> processor) {
        trueChain.remove(processor);
        falseChain.remove(processor);
        return this;
    }

    @Override
    /**
     * clear方法。
     * @return Chain<C>类型返回值
     */
    public Chain<C> clear() {
        trueChain.clear();
        falseChain.clear();
        return this;
    }

    @Override
    /**
     * prepend方法。
     *      * @param other ChainC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> prepend(Chain<C> other) {
        trueChain.prepend(other);
        falseChain.prepend(other);
        return this;
    }

    @Override
    /**
     * append方法。
     *      * @param other ChainC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> append(Chain<C> other) {
        trueChain.append(other);
        falseChain.append(other);
        return this;
    }
}
