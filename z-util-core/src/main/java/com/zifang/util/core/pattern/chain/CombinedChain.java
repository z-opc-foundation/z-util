package com.zifang.util.core.pattern.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 组合链实现
 * <p>
 * 将两个链组合成一个链依次执行
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class CombinedChain<C extends ChainContext<?, ?>> implements Chain<C> {

    private final Chain<C> first;
    private final Chain<C> second;
    private final String name;
    private final List<Processor<C>> processors;

    /**
     * CombinedChain方法。
     * * @param first ChainC类型参数
     *
     * @param second ChainC类型参数
     */
    public CombinedChain(Chain<C> first, Chain<C> second) {
        this.first = first != null ? first : Chain.empty();
        this.second = second != null ? second : Chain.empty();
        this.name = "CombinedChain[" + first.getName() + " + " + second.getName() + "]";
        this.processors = new ArrayList<>();
        this.processors.addAll(this.first.getProcessors());
        this.processors.addAll(this.second.getProcessors());
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
        return Collections.unmodifiableList(processors);
    }

    @Override
    /**
     * process方法。
     *      * @param context C类型参数
     * @return ProcessorResult类型返回值
     */
    public ProcessorResult process(C context) {
        ProcessorResult result = first.process(context);
        if (result.shouldContinue()) {
            return second.process(context);
        }
        return result;
    }

    @Override
    /**
     * addProcessor方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addProcessor(Processor<C> processor) {
        second.addProcessor(processor);
        this.processors.add(processor);
        return this;
    }

    @Override
    /**
     * addFirst方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addFirst(Processor<C> processor) {
        first.addFirst(processor);
        this.processors.add(0, processor);
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
        if (index <= first.size()) {
            first.addAt(index, processor);
        } else {
            second.addAt(index - first.size(), processor);
        }
        this.processors.add(index, processor);
        return this;
    }

    @Override
    /**
     * remove方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> remove(Processor<C> processor) {
        first.remove(processor);
        second.remove(processor);
        this.processors.remove(processor);
        return this;
    }

    @Override
    /**
     * clear方法。
     * @return Chain<C>类型返回值
     */
    public Chain<C> clear() {
        first.clear();
        second.clear();
        this.processors.clear();
        return this;
    }

    @Override
    /**
     * prepend方法。
     *      * @param other ChainC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> prepend(Chain<C> other) {
        first.prepend(other);
        this.processors.addAll(0, other.getProcessors());
        return this;
    }

    @Override
    /**
     * append方法。
     *      * @param other ChainC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> append(Chain<C> other) {
        second.append(other);
        this.processors.addAll(other.getProcessors());
        return this;
    }
}
