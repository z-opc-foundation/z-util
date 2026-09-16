package com.zifang.util.monitor.thread.executor;

import com.zifang.util.monitor.thread.alarm.LogAlarmService;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;

/**
 * MonitorableExecutorTest类。
 */
public class MonitorableExecutorTest {

    @Test
    /**
     * testMonitorableExecutorInterface方法。
     */
    public void testMonitorableExecutorInterface() {
        FixedMonitorableExecutor executor = new FixedMonitorableExecutor(
                createConfigUnit(),
                new java.util.concurrent.LinkedBlockingQueue<>(),
                Executors.defaultThreadFactory()
        );

        assertNotNull(executor);
        assertNotNull(executor.status());
        ExecutorService es = executor;
        es.shutdown();
    }

    private ThreadPoolConfigUnit createConfigUnit() {
        ThreadPoolConfigUnit unit = new ThreadPoolConfigUnit();
        unit.setPoolName("TestPool");
        unit.setPoolSize(2);
        unit.setAlarmService(new LogAlarmService());
        return unit;
    }
}
