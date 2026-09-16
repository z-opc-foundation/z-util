package com.zifang.util.zex.disrupt;

import com.lmax.disruptor.EventFactory;

/**
 * LongEvent工厂类。
 * <p>
 * 产生LongEvent的工厂类，它会在Disruptor系统初始化时，
 * 构造所有的缓冲区中的对象实例（预先分配空间）。
 *
 * @author zifang
 * @version 1.0
 */
public class LongEventFactory implements EventFactory<LongEvent> {

    /**
     * 创建一个新的LongEvent实例。
     *
     * @return 新的LongEvent实例
     */
    @Override
    /**
     * newInstance方法。
     * @return LongEvent类型返回值
     */
    public LongEvent newInstance() {
        return new LongEvent();
    }
}