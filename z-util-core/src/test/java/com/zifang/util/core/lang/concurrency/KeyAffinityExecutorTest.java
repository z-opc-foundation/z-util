package com.zifang.util.core.lang.concurrency;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * KeyAffinityExecutor的单元测试。
 */
public class KeyAffinityExecutorTest {

    @Test
    public void testSameKeyRunsSeriallyInOrder() throws Exception {
        KeyAffinityExecutor executor = new KeyAffinityExecutor(4, "ka-order", 1024);
        final List<Integer> seen = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger();
        int tasks = 50;
        final CountDownLatch done = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++) {
            final int index = i;
            executor.execute("same-key", new Runnable() {
                @Override
                public void run() {
                    synchronized (seen) {
                        seen.add(index);
                    }
                    counter.incrementAndGet();
                    done.countDown();
                }
            });
        }
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertEquals(tasks, counter.get());
        for (int i = 0; i < tasks; i++) {
            assertEquals(Integer.valueOf(i), seen.get(i));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    public void testKeyAlwaysMapsToSameStripe() {
        KeyAffinityExecutor executor = new KeyAffinityExecutor(8);
        int first = executor.stripeOf("instance-42");
        for (int i = 0; i < 20; i++) {
            assertEquals(first, executor.stripeOf("instance-42"));
        }
        assertEquals(8, executor.stripeCount());
        assertEquals(0, executor.stripeOf(null));
        executor.shutdown();
    }

    @Test
    public void testConsecutiveLongKeysSpreadAcrossStripes() {
        KeyAffinityExecutor executor = new KeyAffinityExecutor(16);
        boolean[] hit = new boolean[16];
        for (long i = 0; i < 200; i++) {
            hit[executor.stripeOf(Long.valueOf(i))] = true;
        }
        int used = 0;
        for (boolean b : hit) {
            if (b) {
                used++;
            }
        }
        assertTrue("连号 key 应铺开多条 stripe, 实际只用了 " + used, used > 8);
        executor.shutdown();
    }

    @Test
    public void testSubmitReturnsValue() throws Exception {
        KeyAffinityExecutor executor = new KeyAffinityExecutor(2);
        Future<String> future = executor.submit("k", () -> "value-of-k");
        assertEquals("value-of-k", future.get(5, TimeUnit.SECONDS));
        assertEquals("direct", executor.invoke("k", () -> "direct"));
        executor.shutdown();
    }

    @Test
    public void testInvokeWrapsTaskFailure() {
        KeyAffinityExecutor executor = new KeyAffinityExecutor(2);
        try {
            executor.invoke("k", () -> {
                throw new IllegalStateException("boom");
            });
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getCause() instanceof IllegalStateException);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void testNullTaskIgnoredAndNullKeyRouted() {
        KeyAffinityExecutor executor = new KeyAffinityExecutor(3);
        executor.execute("k", null);
        executor.execute(null, () -> {
        });
        assertEquals(0, executor.pendingTasks());
        executor.shutdown();
        assertTrue(executor.isShutdown());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsNonPositiveStripeCount() {
        new KeyAffinityExecutor(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsNonPositiveQueueCapacity() {
        new KeyAffinityExecutor(2, "ka", 0);
    }
}
