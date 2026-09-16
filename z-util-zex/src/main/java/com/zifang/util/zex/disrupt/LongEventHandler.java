package com.zifang.util.zex.disrupt;


import com.lmax.disruptor.EventHandler;

/**
 * LongEvent事件处理器（消费者）。
 * <p>
 * 消费者实现为EventHandler接口，是Disruptor框架中的类。
 *
 * @author zifang
 * @version 1.0
 */
public class LongEventHandler implements EventHandler<LongEvent> {

    /**
     * 处理事件回调方法。
     *
     * @param event      事件对象
     * @param sequence   事件序列号
     * @param endOfBatch 是否是批次中的最后一个事件
     */
    @Override
    /**
     * onEvent方法。
     *      * @param event LongEvent类型参数
     * @param sequence long类型参数
     * @param endOfBatch boolean类型参数
     */
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
        System.out.println("Event: " + event);
    }
}
