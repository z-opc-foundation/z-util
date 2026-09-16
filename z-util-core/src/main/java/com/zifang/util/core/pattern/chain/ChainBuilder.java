package com.zifang.util.core.pattern.chain;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 链构建器
 * <p>
 * 提供流畅的API来构建复杂的链结构
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class ChainBuilder<C extends ChainContext<?, ?>> implements Chain<C>, Processor<C> {

    private final String name;
    private final List<Processor<C>> processors;
    private final Deque<Chain<C>> chainStack;

    private ChainBuilder(String name) {
        this.name = name;
        this.processors = new ArrayList<>();
        this.chainStack = new ArrayDeque<>();
    }

    private ChainBuilder(String name, List<Processor<C>> processors) {
        this.name = name;
        this.processors = new ArrayList<>(processors);
        this.chainStack = new ArrayDeque<>();
    }

    /**
     * 创建命名的链构建器
     */
    public static <C extends ChainContext<?, ?>> ChainBuilder<C> create(String name) {
        return new ChainBuilder<>(name);
    }

    /**
     * 创建链构建器
     */
    public static <C extends ChainContext<?, ?>> ChainBuilder<C> create() {
        return new ChainBuilder<>("ChainBuilder-" + UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * 开始构建子链
     */
    public ChainBuilder<C> startSubChain() {
        chainStack.push(Chain.empty());
        return this;
    }

    /**
     * 开始构建子链并给个名字
     */
    public ChainBuilder<C> startSubChain(String name) {
        chainStack.push(Chain.named(name));
        return this;
    }

    /**
     * 结束子链并添加到当前链
     */
    @SuppressWarnings("unchecked")
    /**
     * endSubChain方法。
     * @return ChainBuilder<C>类型返回值
     */
    public ChainBuilder<C> endSubChain() {
        if (!chainStack.isEmpty()) {
            Chain<C> subChain = chainStack.pop();
            this.processors.add(subChain);
        }
        return this;
    }

    /**
     * 添加处理器
     */
    public ChainBuilder<C> add(Processor<C> processor) {
        if (processor != null) {
            this.processors.add(processor);
        }
        return this;
    }

    /**
     * 添加多个处理器
     */
    @SafeVarargs
    /**
     * addAll方法。
     *      * @param processors ProcessorC...类型参数
     * @return final ChainBuilder<C>类型返回值
     */
    public final ChainBuilder<C> addAll(Processor<C>... processors) {
        for (Processor<C> p : processors) {
            add(p);
        }
        return this;
    }

    /**
     * 添加条件处理器
     */
    public ChainBuilder<C> addWhen(Predicate<C> predicate, Processor<C> processor) {
        return add(Processor.when(predicate, processor));
    }

    /**
     * 添加过滤器
     */
    public ChainBuilder<C> addFilter(
            Consumer<C> before,
            Consumer<C> after,
            BiFunction<C, ProcessorResult, ProcessorResult> process) {
        return add(FilterProcessor.of(before, after, process));
    }

    /**
     * 添加过滤器（只有前后置处理）
     */
    public ChainBuilder<C> addFilter(Consumer<C> before, Consumer<C> after) {
        return add(FilterProcessor.of(before, after));
    }

    /**
     * 条件分支
     */
    public ChainBuilder<C> branch(Predicate<C> condition, Consumer<BranchChain<C>> branchConsumer) {
        BranchChain<C> branch = new BranchChain<>(condition);
        branchConsumer.accept(branch);
        this.processors.add(branch);
        return this;
    }

    /**
     * IF-ELSE分支
     */
    public ChainBuilder<C> ifElse(Predicate<C> condition,
                                  Processor<C> trueProcessor,
                                  Processor<C> falseProcessor) {
        BranchChain<C> branch = new BranchChain<>(condition);
        branch.whenTrue(trueProcessor);
        branch.whenFalse(falseProcessor);
        this.processors.add(branch);
        return this;
    }

    /**
     * 添加命名处理器
     */
    public ChainBuilder<C> addNamed(String name, Processor<C> processor) {
        return add(new NamedProcessor<>(name, processor));
    }

    /**
     * 设置处理器执行顺序的反转
     */
    public ChainBuilder<C> reversed() {
        Collections.reverse(this.processors);
        return this;
    }

    /**
     * 构建链
     */
    public Chain<C> build() {
        return this;
    }

    // Chain接口实现

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
        return Chain.of(processors.toArray(new Processor[0])).process(context);
    }

    @Override
    /**
     * addProcessor方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addProcessor(Processor<C> processor) {
        return add(processor);
    }

    @Override
    /**
     * addFirst方法。
     *      * @param processor ProcessorC类型参数
     * @return Chain<C>类型返回值
     */
    public Chain<C> addFirst(Processor<C> processor) {
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
        if (index >= 0 && index <= processors.size()) {
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
        this.processors.addAll(other.getProcessors());
        return this;
    }

    /**
     * 执行构建的链
     */
    public ProcessorResult execute(C context) {
        return process(context);
    }

    /**
     * 执行构建的链并返回结果
     */
    public Chain.ChainResult<C> executeWithResult(C context) {
        return Chain.of(processors.toArray(new Processor[0])).executeWithResult(context);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ChainBuilder{" +
                "name='" + name + '\'' +
                ", processors=" + processors.size() +
                '}';
    }

    /**
     * ChainBuilder的运行器封装
     */
    public static class ChainBuilderRunner<C extends ChainContext<?, ?>> {
        private final Chain<C> chain;

        /**
         * ChainBuilderRunner方法。
         * * @param chain ChainC类型参数
         */
        public ChainBuilderRunner(Chain<C> chain) {
            this.chain = chain;
        }

        /**
         * then方法。
         * * @param processor ProcessorC类型参数
         *
         * @return Chain<C>类型返回值
         */
        public Chain<C> then(Processor<C> processor) {
            return chain.addProcessor(processor);
        }

        /**
         * get方法。
         *
         * @return Chain<C>类型返回值
         */
        public Chain<C> get() {
            return chain;
        }

        /**
         * execute方法。
         * * @param context C类型参数
         *
         * @return ProcessorResult类型返回值
         */
        public ProcessorResult execute(C context) {
            return chain.process(context);
        }
    }
}
