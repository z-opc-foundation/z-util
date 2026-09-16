package com.zifang.util.monitor.thread;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * MonitorManagerTest类。
 */
public class MonitorManagerTest {

    private MonitorManager monitorManager;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        monitorManager = new MonitorManager();
    }

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() {
        if (monitorManager != null) {
            monitorManager.shutdown(false);
        }
    }

    @Test
    /**
     * testMonitorManagerCreation方法。
     */
    public void testMonitorManagerCreation() {
        assertNotNull(monitorManager);
    }

    @Test
    /**
     * testAddAllMonitor方法。
     */
    public void testAddAllMonitor() {
        List<Monitorable> monitorables = new ArrayList<>();
        Map<Long, List<Monitorable>> aggregation = new HashMap<>();
        aggregation.put(1000L, monitorables);

        monitorManager.addAllMonitor(aggregation);
        // Should not throw exception
    }

    @Test(expected = IllegalStateException.class)
    /**
     * testAddAllMonitorTwice方法。
     */
    public void testAddAllMonitorTwice() {
        List<Monitorable> monitorables = new ArrayList<>();
        Map<Long, List<Monitorable>> aggregation = new HashMap<>();
        aggregation.put(1000L, monitorables);

        monitorManager.addAllMonitor(aggregation);
        monitorManager.addAllMonitor(aggregation); // Should throw exception
    }

    @Test
    /**
     * testShutdown方法。
     */
    public void testShutdown() {
        monitorManager.shutdown(false);
        monitorManager.shutdown(true);
        // Should not throw exception
    }
}
