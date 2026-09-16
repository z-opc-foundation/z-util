package com.zifang.util.monitor.thread.task;

import com.zifang.util.monitor.thread.Monitorable;
import com.zifang.util.monitor.thread.Status;
import com.zifang.util.monitor.thread.StatusLevel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * MonitorTaskTest类。
 */
public class MonitorTaskTest {

    @Test
    /**
     * testMonitorTaskCreation方法。
     */
    public void testMonitorTaskCreation() {
        List<Monitorable> monitorables = new ArrayList<>();
        MonitorTask task = new MonitorTask(monitorables);
        assertNotNull(task);
    }

    @Test
    /**
     * testMonitorTaskWithMonitorables方法。
     */
    public void testMonitorTaskWithMonitorables() {
        List<Monitorable> monitorables = new ArrayList<>();
        monitorables.add(new TestMonitorable("Test1"));
        monitorables.add(new TestMonitorable("Test2"));

        MonitorTask task = new MonitorTask(monitorables);
        assertNotNull(task);
    }

    @Test
    /**
     * testMonitorTaskRun方法。
     */
    public void testMonitorTaskRun() {
        List<Monitorable> monitorables = new ArrayList<>();
        monitorables.add(new TestMonitorable("Test"));
        MonitorTask task = new MonitorTask(monitorables);
        task.run(); // Should not throw exception
    }

    private static class TestMonitorable implements Monitorable {
        private final String name;

        TestMonitorable(String name) {
            this.name = name;
        }

        @Override
        /**
         * status方法。
         * @return Status类型返回值
         */
        public Status status() {
            Status status = new Status();
            status.setLevel(StatusLevel.OK);
            status.setStatus("OK");
            return status;
        }

        @Override
        /**
         * componentName方法。
         * @return String类型返回值
         */
        public String componentName() {
            return name;
        }

        @Override
        /**
         * alarm方法。
         */
        public void alarm() {
            // do nothing
        }
    }
}
