package com.zifang.util.zex.disrupt;

import com.lmax.disruptor.RingBuffer;

import java.nio.ByteBuffer;

/**
 * LongEvent生产者。
 * <p>
 * 此类负责将数据推入到Disruptor的RingBuffer中。
 * 生产者将数据封装成LongEvent事件，发布到环形缓冲区供消费者消费。
 *
 * @author zifang
 * @version 1.0
 */
public class LongEventProducer {

    /**
     * 环形缓冲区，装载生产好的数据
     */
    private final RingBuffer<LongEvent> ringBuffer;

    /**
     * 构造方法。
     *
     * @param ringBuffer 环形缓冲区
     */
    public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 将数据推入到缓冲区的方法。
     * <p>
     * 将数据装载到ringBuffer中，只有发布后的数据才会真正被消费者看见。
     *
     * @param bb 字节缓冲区，包含要设置的数据
     */
    public void onData(ByteBuffer bb) {
        long sequence = ringBuffer.next(); // 获取下一个可用的序列号
        try {
            LongEvent event = ringBuffer.get(sequence); // 通过序列号获取空闲可用的LongEvent
            event.set(bb.getLong(0)); // 设置数值
        } finally {
            ringBuffer.publish(sequence); // 数据发布
        }
    }
}