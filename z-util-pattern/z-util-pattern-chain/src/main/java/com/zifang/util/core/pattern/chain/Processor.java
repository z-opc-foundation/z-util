package com.zifang.util.core.pattern.chain;

import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * 处理器接口
 * <p>
 * 定义链中单个处理单元的行为
 *
 * @param <C> 上下文类型
 * @author zifang
 */
@FunctionalInterface
/**
 * Processor接口。
 */
public interface Processor<C extends ChainContext<?, ?>> {

    /**
     * 创建一个简单处理器，只执行给定动作（返回CONTINUE）
     */
    static <C extends ChainContext<?, ?>> Processor<C> fromConsumer(java.util.function.Consumer<C> action) {
        return context -> {
            action.accept(context);
            return ProcessorResult.CONTINUE;
        };
    }

    /**
     * 创建一个过滤器处理器 - 支持前置/后置处理
     */
    static <C extends ChainContext<?, ?>> FilterProcessor<C> filter(
            java.util.function.Consumer<C> before,
            java.util.function.Consumer<C> after,
            BiFunction<C, ProcessorResult, ProcessorResult> process) {
        return new FilterProcessor<>(before, after, process);
    }

    /**
     * 创建一个条件处理器 - 满足条件才执行
     */
    static <C extends ChainContext<?, ?>> Processor<C> when(
            Predicate<C> predicate,
            Processor<C> processor) {
        return context -> {
            if (predicate.test(context)) {
                return processor.process(context);
            }
            return ProcessorResult.SKIP;
        };
    }

    /**
     * 执行处理
     *
     * @param context 上下文
     * @return 处理结果
     */
    ProcessorResult process(C context);

    /**
     * 组合两个处理器，按顺序执行
     */
    @SuppressWarnings("unchecked")
    default Processor<C> andThen(Processor<? super C> next) {
        return context -> {
            ProcessorResult result = this.process(context);
            if (result.shouldContinue()) {
                return ((Processor<C>) next).process(context);
            }
            return result;
        };
    }

    /**
     * 在此处理器之后添加另一个处理器
     */
    default ChainBuilder.ChainBuilderRunner<C> addNext(Processor<? super C> next) {
        return new ChainBuilder.ChainBuilderRunner<>((ChainBuilder<C>) this.andThen(next));
    }

    /**
     * 将此处理器转换为链
     */
    default Chain<C> toChain() {
        return new SimpleChain<>(this);
    }
}
