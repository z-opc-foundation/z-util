package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class CounterTest {

    @Test
    public void testBasicIncrement() {
        Counter c = new Counter();
        assertEquals(0, c.get());
        assertEquals(1, c.increment());
        assertEquals(2, c.increment());
        assertEquals(2, c.get());
    }

    @Test
    public void testDecrement() {
        Counter c = new Counter();
        c.increment(10);
        c.decrement();
        assertEquals(9, c.get());
    }

    @Test
    public void testConcurrent() throws InterruptedException {
        final Counter c = new Counter();
        final int N = 1000;
        Thread[] threads = new Thread[10];
        AtomicInteger errors = new AtomicInteger();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < N; j++) c.increment();
                } catch (Throwable t) {
                    errors.incrementAndGet();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        assertEquals(0, errors.get());
        assertEquals(10L * N, c.get());
    }

    @Test
    public void testReset() {
        Counter c = new Counter();
        c.increment(5);
        long old = c.reset();
        assertEquals(5, old);
        assertEquals(0, c.get());
    }
}