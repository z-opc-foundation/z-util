package com.zifang.util.core.pattern.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 简单链实现
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class SimpleChain<C extends ChainContext<?, ?>> implements Chain<C> {

    private final String name;
    private final List<Processor<C>> processors;

    /**
     * SimpleChain方法。
     */
    public SimpleChain() {
        this("SimpleChain-" + UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * SimpleChain方法。
     * * @param name String类型参数
     */
    public SimpleChain(String name) {
        this.name = name;
        this.processors = new ArrayList<>();
    }

    /**
     * SimpleChain方法。
     * * @param processor ProcessorC类型参数
     */
    public SimpleChain(Processor<C> processor) {
        this();
        if (processor != null) {
            this.processors.add(processor);
        }
    }

    /**
     * SimpleChain方法。
     * * @param name String类型参数
     *
     * @param processor ProcessorC类型参数
     */
    public SimpleChain(String name, Processor<C> processor) {
        this.name = name;
        this.processors = new ArrayList<>();
        if (processor != null) {
            this.processors.add(processor);
        }
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
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        ProcessorResult lastResult = ProcessorResult.CONTINUE;

        for (Processor<C> processor : processors) {
            lastResult = processor.process(context);
            if (!lastResult.shouldContinue()) {
                break;
            }
        }

        return lastResult;
    }

    @Override
    /**
     * addProcessor方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addProcessor(Processor<C> processor) {
        if (processor != null) {
            this.processors.add(processor);
        }
        return this;
    }

    @Override
    /**
     * addFirst方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addFirst(Processor<C> processor) {
        if (processor != null) {
            this.processors.add(0, processor);
        }
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
        if (processor != null && index >= 0 && index <= processors.size()) {
            this.processors.add(index, processor);
        }
        return this;
    }

    @Override
    /**
     * remove方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> remove(Processor<C> processor) {
        this.processors.remove(processor);
        return this;
    }

    @Override
    /**
     * clear方法。
     * @return Chain<C>类型返回值
     */
    public Chain<C> clear() {
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
        if (other != null && !other.isEmpty()) {
            List<Processor<C>> otherProcessors = new ArrayList<>(other.getProcessors());
            otherProcessors.addAll(this.processors);
            this.processors.clear();
            this.processors.addAll(otherProcessors);
        }
        return this;
    }

    @Override
    /**
     * append方法。
     *      * @param other ChainC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> append(Chain<C> other) {
        if (other != null && !other.isEmpty()) {
            this.processors.addAll(other.getProcessors());
        }
        return this;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "SimpleChain{" +
                "name='" + name + '\'' +
                ", processors=" + processors.size() +
                '}';
    }
}
