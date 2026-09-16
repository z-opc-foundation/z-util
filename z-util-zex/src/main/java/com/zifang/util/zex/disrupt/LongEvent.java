package com.zifang.util.zex.disrupt;

/**
 * LongEvent类，代表Disruptor中的数据事件。
 * <p>
 * 此类用于在Disruptor框架中封装要传递的数据。
 *
 * @author zifang
 * @version 1.0
 */
public class LongEvent {
    private long value;

    /**
     * 设置事件的值。
     *
     * @param value 事件值
     */
    public void set(long value) {
        this.value = value;
    }
}