package com.zifang.util.core.lang.concurrency;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * AsyncUtilTest类。
 */
public class AsyncUtilTest {

    @Test
    /**
     * testExecuteThrown方法。
     */
    public void testExecuteThrown() {
        List<Integer> result = AsyncUtil.executeThrown(
                () -> 1,
                () -> 2,
                () -> 3);
        // 结果与提交顺序一致
        assertEquals(Arrays.asList(1, 2, 3), result);
        // 空入参返回空列表
        assertTrue(AsyncUtil.executeThrown().isEmpty());
    }

    @Test
    /**
     * testExecuteThrown_ExceptionPropagated方法。
     */
    public void testExecuteThrown_ExceptionPropagated() {
        boolean thrown = false;
        try {
            AsyncUtil.executeThrown(() -> 1, () -> {
                throw new IllegalStateException("boom");
            });
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    /**
     * testExecute方法。
     */
    public void testExecute() {
        List<String> result = AsyncUtil.execute(
                () -> "a",
                () -> {
                    throw new IllegalStateException("boom");
                },
                () -> "c");
        // 异常任务位置记为null，其余结果不受影响
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("c", result.get(2));
        assertTrue(AsyncUtil.execute().isEmpty());
    }

    @Test
    /**
     * testExecute_WithTimeout方法。
     */
    public void testExecute_WithTimeout() {
        Supplier<String> slow = () -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "slow";
        };
        // 超时任务记为null，快速任务正常返回
        List<String> result = AsyncUtil.execute(200, TimeUnit.MILLISECONDS, () -> "fast", slow);
        assertEquals(2, result.size());
        assertEquals("fast", result.get(0));
        assertNull(result.get(1));
    }

    @Test
    /**
     * testExecuteRunnable方法。
     */
    public void testExecuteRunnable() throws Exception {
        AtomicBoolean executed = new AtomicBoolean(false);
        AsyncUtil.executeRunnable(() -> executed.set(true));
        // 等待异步任务完成
        for (int i = 0; i < 100 && !executed.get(); i++) {
            Thread.sleep(20);
        }
        assertTrue(executed.get());
        // 异常任务被吞掉不影响调用线程
        AsyncUtil.executeRunnable(() -> {
            throw new IllegalStateException("boom");
        });
    }

    @Test
    /**
     * testExecute_ConcurrentFasterThanSerial方法。
     */
    public void testExecute_ConcurrentFasterThanSerial() {
        Supplier<Integer> sleeper = () -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 1;
        };
        long start = System.currentTimeMillis();
        List<Integer> result = AsyncUtil.execute(sleeper, sleeper, sleeper);
        long elapsed = System.currentTimeMillis() - start;
        assertEquals(Arrays.asList(1, 1, 1), result);
        // 并发执行总耗时应明显小于串行的600ms
        assertTrue("elapsed=" + elapsed, elapsed < 500);
    }
}
