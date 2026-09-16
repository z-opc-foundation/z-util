package com.zifang.util.core.pattern.chain;

import java.util.List;
import java.util.function.Predicate;

/**
 * 链接口
 * <p>
 * 定义链的执行行为，支持执行、组合、条件执行等操作
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public interface Chain<C extends ChainContext<?, ?>> extends Processor<C> {

    /**
     * 创建空链
     */
    static <C extends ChainContext<?, ?>> Chain<C> empty() {
        return new SimpleChain<>();
    }

    /**
     * 创建只有一个处理器的链
     */
    static <C extends ChainContext<?, ?>> Chain<C> of(Processor<C> processor) {
        return new SimpleChain<>(processor);
    }

    /**
     * 创建链并添加多个处理器
     */
    @SafeVarargs
    static <C extends ChainContext<?, ?>> Chain<C> of(Processor<C>... processors) {
        Chain<C> chain = new SimpleChain<>();
        for (Processor<C> p : processors) {
            chain.addProcessor(p);
        }
        return chain;
    }

    /**
     * 创建一个命名链
     */
    static <C extends ChainContext<?, ?>> Chain<C> named(String name) {
        return new NamedChain<>(name);
    }

    /**
     * 创建一个分支链 - 根据条件选择子链
     */
    static <C extends ChainContext<?, ?>> BranchChain<C> branch(Predicate<C> condition) {
        return new BranchChain<>(condition);
    }

    /**
     * 创建一个链构建器
     */
    static <C extends ChainContext<?, ?>> ChainBuilder<C> builder() {
        return ChainBuilder.create();
    }

    /**
     * 获取链名称
     */
    String getName();

    /**
     * 获取处理器列表
     */
    List<Processor<C>> getProcessors();

    /**
     * 获取处理器数量
     */
    default int size() {
        return getProcessors().size();
    }

    /**
     * 是否为空链
     */
    default boolean isEmpty() {
        return getProcessors().isEmpty();
    }

    /**
     * 执行链
     */
    @Override
    ProcessorResult process(C context);

    /**
     * 在链末尾添加处理器
     */
    Chain<C> addProcessor(Processor<C> processor);

    /**
     * 在链开头添加处理器
     */
    Chain<C> addFirst(Processor<C> processor);

    /**
     * 在指定位置添加处理器
     */
    Chain<C> addAt(int index, Processor<C> processor);

    /**
     * 移除指定处理器
     */
    Chain<C> remove(Processor<C> processor);

    /**
     * 清除所有处理器
     */
    Chain<C> clear();

    /**
     * 在当前链之前添加另一个链
     */
    Chain<C> prepend(Chain<C> other);

    /**
     * 在当前链之后添加另一个链
     */
    Chain<C> append(Chain<C> other);

    /**
     * 组合两个链
     */
    @SuppressWarnings("unchecked")
    default Chain<C> andThen(Chain<? super C> other) {
        return new CombinedChain<>((Chain<C>) this, (Chain<C>) other);
    }

    /**
     * 执行链并收集结果
     */
    default ChainResult<C> executeWithResult(C context) {
        ProcessorResult result = process(context);
        return new ChainResult<>(this, context, result);
    }

    /**
     * 链执行结果
     */
    class ChainResult<C extends ChainContext<?, ?>> {
        private final Chain<C> chain;
        private final C context;
        private final ProcessorResult result;

        /**
         * ChainResult方法。
         * * @param chain ChainC类型参数
         *
         * @param context C类型参数
         * @param result  ProcessorResult类型参数
         */
        public ChainResult(Chain<C> chain, C context, ProcessorResult result) {
            this.chain = chain;
            this.context = context;
            this.result = result;
        }

        /**
         * getChain方法。
         *
         * @return Chain<C>类型返回值
         */
        public Chain<C> getChain() {
            return chain;
        }

        /**
         * getContext方法。
         *
         * @return C类型返回值
         */
        public C getContext() {
            return context;
        }

        /**
         * getResult方法。
         *
         * @return ProcessorResult类型返回值
         */
        public ProcessorResult getResult() {
            return result;
        }

        /**
         * isSuccess方法。
         *
         * @return boolean类型返回值
         */
        public boolean isSuccess() {
            return result.isSuccess();
        }

        /**
         * isFinished方法。
         *
         * @return boolean类型返回值
         */
        public boolean isFinished() {
            return result.isFinished();
        }

        /**
         * isContinued方法。
         *
         * @return boolean类型返回值
         */
        public boolean isContinued() {
            return result.shouldContinue();
        }
    }
}
