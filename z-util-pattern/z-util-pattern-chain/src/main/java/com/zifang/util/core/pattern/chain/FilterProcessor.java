package com.zifang.util.core.pattern.chain;

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 过滤器处理器
 * <p>
 * 支持前置处理、后置处理和核心处理逻辑
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class FilterProcessor<C extends ChainContext<?, ?>> implements Processor<C> {

    private final Consumer<C> before;
    private final Consumer<C> after;
    private final BiFunction<C, ProcessorResult, ProcessorResult> process;

    /**
     * FilterProcessor方法。
     * * @param before ConsumerC类型参数
     *
     * @param after   ConsumerC类型参数
     * @param process BiFunctionC,类型参数
     */
    public FilterProcessor(Consumer<C> before, Consumer<C> after,
                           BiFunction<C, ProcessorResult, ProcessorResult> process) {
        this.before = before != null ? before : ctx -> {
        };
        this.after = after != null ? after : ctx -> {
        };
        this.process = process != null ? process : (ctx, result) -> result;
    }

    /**
     * 创建一个简单的过滤器，只执行前后置处理
     */
    public static <C extends ChainContext<?, ?>> FilterProcessor<C> of(
            Consumer<C> before,
            Consumer<C> after) {
        return new FilterProcessor<>(before, after, (ctx, result) -> result);
    }

    /**
     * 创建一个过滤器，包含前置处理、核心处理和后置处理
     */
    public static <C extends ChainContext<?, ?>> FilterProcessor<C> of(
            Consumer<C> before,
            Consumer<C> after,
            BiFunction<C, ProcessorResult, ProcessorResult> process) {
        return new FilterProcessor<>(before, after, process);
    }

    @Override
    /**
     * process方法。
     *      * @param context C类型参数
     * @return ProcessorResult类型返回值
     */
    public ProcessorResult process(C context) {
        before.accept(context);

        ProcessorResult result = ProcessorResult.CONTINUE;
        Exception exception = null;

        try {
            result = process.apply(context, result);
        } catch (Exception e) {
            exception = e;
        }

        try {
            after.accept(context);
        } catch (Exception e) {
            if (exception == null) {
                exception = e;
            }
        }

        if (exception != null) {
            throw new RuntimeException("Filter processing failed", exception);
        }

        return result;
    }
}
