package com.zifang.util.core.lang.concurrency;

import org.junit.After;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * MdcPropagation与MDC线程池的单元测试。
 */
public class MdcPropagationTest {

    @After
    public void clearMdc() {
        MDC.clear();
    }

    @Test
    public void testPoolPropagatesContextToWorkerThread() throws Exception {
        MDC.put("traceId", "trace-pool");
        MdcThreadPoolExecutor pool = new MdcThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final String[] observed = new String[1];
            pool.execute(() -> {
                observed[0] = MDC.get("traceId");
                latch.countDown();
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals("trace-pool", observed[0]);

            Future<String> future = pool.submit(() -> MDC.get("traceId"));
            assertEquals("trace-pool", future.get(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testWorkerContextIsCleanedBetweenTasks() throws Exception {
        MDC.put("traceId", "first");
        MdcThreadPoolExecutor pool = new MdcThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        try {
            Future<String> withContext = pool.submit(() -> MDC.get("traceId"));
            assertEquals("first", withContext.get(5, TimeUnit.SECONDS));
            MDC.clear();
            Future<String> withoutContext = pool.submit(() -> MDC.get("traceId"));
            assertNull("同一个 worker 线程复用后不应残留上一条任务的 traceId",
                    withoutContext.get(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testCallerRunsKeepsCallerContext() throws Exception {
        MdcThreadPoolExecutor pool = new MdcThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), (RejectedExecutionHandler) new ThreadPoolExecutor.CallerRunsPolicy());
        try {
            final CountDownLatch occupy = new CountDownLatch(1);
            final CountDownLatch release = new CountDownLatch(1);
            final String[] inlineObserved = new String[1];
            MDC.put("traceId", "caller-trace");
            // 任务 A 占住唯一的线程
            pool.execute(() -> {
                occupy.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            assertTrue(occupy.await(5, TimeUnit.SECONDS));
            // 任务 B 占满容量为 1 的队列
            pool.execute(() -> {
            });
            // 任务 C 被拒绝，只能在调用线程上就地执行
            pool.execute(() -> inlineObserved[0] = MDC.get("traceId"));
            assertEquals("CallerRuns 时任务应仍带调用线程的上下文", "caller-trace", inlineObserved[0]);
            assertEquals("调用线程自己的上下文不能被任务收尾清掉", "caller-trace", MDC.get("traceId"));
            release.countDown();
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testViewDecoratesPlainExecutor() throws Exception {
        MDC.put("traceId", "viewed");
        Executor raw = new Executor() {
            @Override
            public void execute(Runnable command) {
                CountDownLatch latch = new CountDownLatch(1);
                Thread thread = new Thread(() -> {
                    command.run();
                    latch.countDown();
                }, "mdc-view-thread");
                thread.start();
                try {
                    assertTrue(latch.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        final String[] observed = new String[1];
        raw = MdcPropagation.view(raw);
        raw.execute(() -> observed[0] = MDC.get("traceId"));
        assertEquals("viewed", observed[0]);
    }

    @Test
    public void testScheduledPoolPropagatesContext() throws Exception {
        MDC.put("traceId", "tick");
        MdcScheduledThreadPoolExecutor pool = new MdcScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "mdc-sched-test");
                thread.setDaemon(true);
                return thread;
            }
        });
        try {
            final CountDownLatch latch = new CountDownLatch(3);
            final List<String> observed = new ArrayList<>();
            Runnable probe = () -> {
                String value = MDC.get("traceId");
                observed.add(value == null ? "MISSING" : value);
                latch.countDown();
            };
            ScheduledFuture<?> oneShot = pool.schedule(probe, 10, TimeUnit.MILLISECONDS);
            pool.execute(probe);
            pool.submit(probe);
            oneShot.get(5, TimeUnit.SECONDS);
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals("3 条入口都应带上提交时的 traceId: " + observed,
                    java.util.Arrays.asList("tick", "tick", "tick"), observed);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testScheduledFixedRateKeepsSubmitTimeContext() throws Exception {
        MDC.put("traceId", "interval");
        MdcScheduledThreadPoolExecutor pool = new MdcScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, "mdc-fixed-rate-test");
            thread.setDaemon(true);
            return thread;
        });
        try {
            final CountDownLatch latch = new CountDownLatch(2);
            final StringBuilder observed = new StringBuilder();
            pool.scheduleAtFixedRate(() -> {
                observed.append(MDC.get("traceId")).append(';');
                latch.countDown();
            }, 5, 10, TimeUnit.MILLISECONDS);
            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals("interval;interval;", observed.toString());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void testSnapshotAndRestore() {
        MDC.put("a", "1");
        Map<String, String> snapshot = MdcPropagation.snapshot();
        MDC.clear();
        assertNull(MDC.get("a"));
        MdcPropagation.restore(snapshot);
        assertEquals("1", MDC.get("a"));
        MdcPropagation.restore(null);
        assertNull(MDC.get("a"));
    }

    @Test
    public void testWrapNullReturnsNull() {
        assertNull(MdcPropagation.wrap((Runnable) null));
        assertNull(MdcPropagation.wrap((java.util.concurrent.Callable<String>) null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testViewRejectsNullExecutor() {
        MdcPropagation.view(null);
    }

    /**
     * 用 SynchronousQueue 验证不包装的执行器确实会丢上下文（保证上面的测试不是假绿）。
     */
    @Test
    public void testPlainPoolLosesContext() throws Exception {
        MDC.put("traceId", "lost");
        ThreadPoolExecutor plain = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>());
        try {
            Future<String> future = plain.submit(() -> MDC.get("traceId"));
            assertNull("未做传播的原生线程池本就该丢上下文", future.get(5, TimeUnit.SECONDS));
        } finally {
            plain.shutdownNow();
        }
    }
}
