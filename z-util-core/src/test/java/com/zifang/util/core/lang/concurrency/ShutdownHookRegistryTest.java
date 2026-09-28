package com.zifang.util.core.lang.concurrency;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ShutdownHookRegistry的单元测试。
 */
public class ShutdownHookRegistryTest {

    @Before
    public void resetRegistry() {
        ShutdownHookRegistry.clear();
    }

    @Test
    public void testRegisterIsIdempotentByKey() {
        final int[] runs = new int[1];
        assertTrue(ShutdownHookRegistry.register("stop-server", () -> runs[0]++));
        assertFalse("同 key 重复注册必须被忽略", ShutdownHookRegistry.register("stop-server", () -> runs[0]++));
        assertEquals(1, ShutdownHookRegistry.size());
        ShutdownHookRegistry.runAll();
        assertEquals(1, runs[0]);
    }

    @Test
    public void testRunAllIsReverseOrder() {
        final List<String> order = new ArrayList<>();
        ShutdownHookRegistry.register("first", () -> order.add("first"));
        ShutdownHookRegistry.register("second", () -> order.add("second"));
        ShutdownHookRegistry.register("third", () -> order.add("third"));
        ShutdownHookRegistry.runAll();
        assertEquals("后注册的先关", order, new ArrayList<>(java.util.Arrays.asList("third", "second", "first")));
    }

    @Test
    public void testFailingHookDoesNotStopOthers() {
        final List<String> done = new ArrayList<>();
        ShutdownHookRegistry.register("boom", () -> {
            throw new IllegalStateException("hook failed");
        });
        ShutdownHookRegistry.register("after", () -> {
            synchronized (done) {
                done.add("after");
            }
        });
        ShutdownHookRegistry.runAll();
        assertEquals(1, done.size());
    }

    @Test
    public void testTimeoutDoesNotBlockRemainingHooks() throws Exception {
        final List<String> done = new ArrayList<>();
        long begin = System.nanoTime();
        ShutdownHookRegistry.register("hang", new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, 50);
        ShutdownHookRegistry.register("quick", () -> {
            synchronized (done) {
                done.add("quick");
            }
        });
        ShutdownHookRegistry.runAll();
        long costMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin);
        assertEquals(1, done.size());
        assertTrue("超时上限没起作用，耗时 " + costMillis + "ms", costMillis < 5_000);
    }

    @Test
    public void testRemoveAndSize() {
        ShutdownHookRegistry.register("a", () -> {
        });
        ShutdownHookRegistry.register("b", () -> {
        });
        assertEquals(2, ShutdownHookRegistry.size());
        assertTrue(ShutdownHookRegistry.remove("a"));
        assertFalse(ShutdownHookRegistry.remove("a"));
        assertEquals(1, ShutdownHookRegistry.size());
        ShutdownHookRegistry.clear();
        assertEquals(0, ShutdownHookRegistry.size());
    }

    @Test
    public void testUnnamedRegisterGeneratesDistinctKeys() {
        assertTrue(ShutdownHookRegistry.register(() -> {
        }));
        assertTrue(ShutdownHookRegistry.register(() -> {
        }));
        assertEquals(2, ShutdownHookRegistry.size());
    }

    @Test
    public void testNullHookAndNullKeyTolerated() {
        assertFalse(ShutdownHookRegistry.register("x", null));
        assertTrue(ShutdownHookRegistry.register(null, () -> {
        }));
        assertEquals(1, ShutdownHookRegistry.size());
    }

    @Test
    public void testRunAllKeepsEntriesForRepeatShutdownSimulation() {
        final int[] runs = new int[1];
        ShutdownHookRegistry.register("counter", () -> {
            synchronized (runs) {
                runs[0]++;
            }
        });
        ShutdownHookRegistry.runAll();
        ShutdownHookRegistry.runAll();
        assertEquals("runAll 不清表，测试里可反复模拟关停", 2, runs[0]);
        assertEquals(1, ShutdownHookRegistry.size());
    }
}
