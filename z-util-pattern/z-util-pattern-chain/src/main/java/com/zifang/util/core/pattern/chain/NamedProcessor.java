package com.zifang.util.core.pattern.chain;

/**
 * 命名处理器
 * <p>
 * 为处理器添加名称，方便调试和日志记录
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class NamedProcessor<C extends ChainContext<?, ?>> implements Processor<C> {

    private final String name;
    private final Processor<C> delegate;

    /**
     * NamedProcessor方法。
     * * @param name String类型参数
     *
     * @param delegate ProcessorC类型参数
     */
    public NamedProcessor(String name, Processor<C> delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    @Override
    /**
     * process方法。
     *      * @param context C类型参数
     * @return ProcessorResult类型返回值
     */
    public ProcessorResult process(C context) {
        return delegate.process(context);
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getDelegate方法。
     *
     * @return Processor<C>类型返回值
     */
    public Processor<C> getDelegate() {
        return delegate;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "NamedProcessor{name='" + name + "'}";
    }
}
