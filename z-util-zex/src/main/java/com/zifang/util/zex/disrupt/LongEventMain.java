package com.zifang.util.zex.disrupt;

/**
 * Disruptor高性能消息框架示例类。
 * <p>
 * 此类展示了LMAX Disruptor的使用方式。
 * Disruptor是一种高性能的消息队列框架。
 *
 * @author zifang
 * @version 1.0
 */

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * LongEventMain类。
 */
public class LongEventMain {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        // Executor that will be used to construct new threads for consumers
        //创建线程池
        Executor executor = Executors.newCachedThreadPool();

        // The factory for the event
        //事件工厂
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        //ringBuffer 的缓冲区的大小是1024
        int bufferSize = 1024;

        // Construct the Disruptor
        //创建一个disruptor
        //ProducerType.MULTI:创建一个环形缓冲区支持多事件发布到一个环形缓冲区
        Disruptor<LongEvent> disruptor = new Disruptor<>(factory, bufferSize, executor, ProducerType.MULTI, new BlockingWaitStrategy());

        // Connect the handler
        //创建一个消费者
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        //启动并初始化disruptor
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        //获取已经初始化好的ringBuffer
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        ////获取已经初始化好的ringBuffer
        LongEventProducer producer = new LongEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            //存入数据
            bb.putLong(0, l);
            producer.onData(bb);
            Thread.sleep(1000);
        }
    }
}