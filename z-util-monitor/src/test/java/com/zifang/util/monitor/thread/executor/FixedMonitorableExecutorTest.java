package com.zifang.util.monitor.thread.executor;

import com.zifang.util.monitor.thread.Status;
import com.zifang.util.monitor.thread.StatusLevel;
import com.zifang.util.monitor.thread.alarm.LogAlarmService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * FixedMonitorableExecutorTest类。
 */
public class FixedMonitorableExecutorTest {

    private FixedMonitorableExecutor executor;
    private ThreadPoolConfigUnit configUnit;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        configUnit = new ThreadPoolConfigUnit();
        configUnit.setPoolName("TestExecutor");
        configUnit.setPoolSize(2);
        configUnit.setAlarmService(new LogAlarmService());
        executor = new FixedMonitorableExecutor(
                configUnit,
                new java.util.concurrent.LinkedBlockingQueue<>(),
                Executors.defaultThreadFactory()
        );
    }

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    @Test
    /**
     * testExecutorCreation方法。
     */
    public void testExecutorCreation() {
        assertNotNull(executor);
        assertFalse(executor.isShutdown());
        assertFalse(executor.isTerminated());
    }

    @Test
    /**
     * testExecuteTask方法。
     */
    public void testExecuteTask() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        executor.execute(() -> counter.incrementAndGet());
        executor.execute(() -> counter.incrementAndGet());
        executor.awaitTermination(1, TimeUnit.SECONDS);
        assertEquals(2, counter.get());
    }

    @Test
    /**
     * testSubmitTask方法。
     */
    public void testSubmitTask() throws Exception {
        AtomicInteger counter = new AtomicInteger(0);
        executor.submit(() -> counter.incrementAndGet()).get();
        executor.submit(() -> counter.incrementAndGet()).get();
        assertEquals(2, counter.get());
    }

    @Test
    /**
     * testStatus方法。
     */
    public void testStatus() {
        Status status = executor.status();
        assertNotNull(status);
        assertEquals(StatusLevel.OK, status.getLevel());
    }

    @Test
    /**
     * testComponentName方法。
     */
    public void testComponentName() {
        assertEquals("TestExecutor", executor.componentName());
    }

    @Test
    /**
     * testAlarm方法。
     */
    public void testAlarm() {
        // Should not throw exception
        executor.alarm();
    }

    @Test
    /**
     * testShutdown方法。
     */
    public void testShutdown() {
        executor.shutdown();
        assertTrue(executor.isShutdown());
    }

    @Test
    /**
     * testShutdownNow方法。
     */
    public void testShutdownNow() {
        executor.shutdownNow();
        assertTrue(executor.isShutdown());
    }
}
