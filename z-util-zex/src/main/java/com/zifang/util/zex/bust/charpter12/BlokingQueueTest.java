package com.zifang.util.zex.bust.charpter12;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * BlokingQueueTest类。
 */
public class BlokingQueueTest {

    @Test
    /**
     * test1_1方法。
     */
    public void test1_1() {
        BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(2);
        blockingQueue.add(1);
        blockingQueue.add(1);
        blockingQueue.add(1);
    }

    @Test
    /**
     * test2_1方法。
     */
    public void test2_1() {
        ArrayBlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(2);
        blockingQueue.add(1);
        blockingQueue.add(1);
        blockingQueue.remove();
        blockingQueue.remove();
        blockingQueue.remove();
    }

    @Test
    /**
     * test3_1方法。
     */
    public void test3_1() {
        ArrayBlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<Integer>(2);
        blockingQueue.element();
    }
}
