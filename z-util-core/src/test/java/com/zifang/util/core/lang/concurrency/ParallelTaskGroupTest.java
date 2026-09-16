package com.zifang.util.core.lang.concurrency;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * ParallelTaskGroup测试：任务登记、依赖链、外部Future纳入与统一等待。
 */
public class ParallelTaskGroupTest {

    private static final Executor DIRECT = Runnable::run;

    @Test
    public void testSubmitAndAwaitAll() {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        CompletableFuture<Integer> f1 = group.submit(() -> 1);
        CompletableFuture<Integer> f2 = group.submit("task-b", () -> 2);
        CompletableFuture<Integer> f3 = group.submit(() -> 3);
        group.awaitAll();
        assertTrue(f1.isDone());
        assertTrue(f2.isDone());
        assertTrue(f3.isDone());
        assertEquals(Integer.valueOf(1), f1.join());
        assertEquals(Integer.valueOf(2), f2.join());
        assertEquals(Integer.valueOf(3), f3.join());
    }

    @Test
    public void testSubmitAfterChaining() {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        CompletableFuture<Integer> base = group.submit(() -> 20);
        CompletableFuture<Integer> mapped = group.submitAfter(base, v -> v + 22);
        CompletableFuture<String> mappedAgain = group.submitAfter("double-map", mapped, v -> "result-" + v);
        group.awaitAll();
        assertEquals(Integer.valueOf(42), mapped.join());
        assertEquals("result-42", mappedAgain.join());
    }

    @Test
    public void testRegisterExternalFuture() {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        CompletableFuture<String> external = CompletableFuture.completedFuture("external");
        group.register(external);
        CompletableFuture<Integer> inner = group.submit(() -> 7);
        group.awaitAll();
        assertEquals("external", external.join());
        assertEquals(Integer.valueOf(7), inner.join());
        // register 返回自身，支持链式调用
        CompletableFuture<Integer> another = CompletableFuture.completedFuture(8);
        group.register(another);
        group.awaitAll();
        assertEquals(Integer.valueOf(8), another.join());
    }

    @Test
    public void testAwaitAllEmpty() {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        // 空任务组立即返回，不抛异常
        group.awaitAll();
    }

    @Test
    public void testAwaitAllWithTimeoutSuccess() throws Exception {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        group.submit(() -> 1);
        group.submit(() -> 2);
        group.awaitAll(1, TimeUnit.SECONDS);
    }

    @Test
    public void testAwaitAllTimeoutExceeded() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            ParallelTaskGroup group = ParallelTaskGroup.on(pool);
            group.submit(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return 1;
            });
            try {
                group.awaitAll(50, TimeUnit.MILLISECONDS);
                fail("expected TimeoutException");
            } catch (TimeoutException e) {
                // 预期超时
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testFailurePropagatesOnJoin() {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        CompletableFuture<Integer> failed = group.submit("boom", () -> {
            throw new IllegalStateException("boom");
        });
        try {
            group.awaitAll();
            fail("expected CompletionException");
        } catch (CompletionException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
        try {
            failed.join();
            fail("expected CompletionException");
        } catch (CompletionException e) {
            assertEquals("boom", e.getCause().getMessage());
        }
    }

    @Test
    public void testSubmitAfterFailurePropagates() {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        CompletableFuture<Integer> base = group.submit(() -> 1);
        CompletableFuture<Integer> mapped = group.submitAfter("map-boom", base, v -> {
            throw new IllegalArgumentException("map failed");
        });
        try {
            group.awaitAll();
            fail("expected CompletionException");
        } catch (CompletionException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
        assertTrue(mapped.isCompletedExceptionally());
    }

    @Test(expected = NullPointerException.class)
    public void testNullExecutorRejected() {
        ParallelTaskGroup.on(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullSupplierRejected() {
        ParallelTaskGroup group = ParallelTaskGroup.on(DIRECT);
        group.submit(null);
    }
}
