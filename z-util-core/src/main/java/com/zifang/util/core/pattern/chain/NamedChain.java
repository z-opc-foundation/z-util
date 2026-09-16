package com.zifang.util.core.pattern.chain;

/**
 * 命名链实现
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class NamedChain<C extends ChainContext<?, ?>> extends SimpleChain<C> {

    /**
     * NamedChain方法。
     * * @param name String类型参数
     */
    public NamedChain(String name) {
        super(name);
    }

    /**
     * NamedChain方法。
     * * @param name String类型参数
     *
     * @param processor ProcessorC类型参数
     */
    public NamedChain(String name, Processor<C> processor) {
        super(name, processor);
    }
}
