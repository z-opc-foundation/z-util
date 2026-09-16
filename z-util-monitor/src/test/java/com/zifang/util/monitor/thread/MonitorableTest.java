package com.zifang.util.monitor.thread;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * MonitorableTest类。
 */
public class MonitorableTest {

    @Test
    /**
     * testMonitorableInterfaceExists方法。
     */
    public void testMonitorableInterfaceExists() {
        Monitorable monitorable = new Monitorable() {
            @Override
            /**
             * status方法。
             * @return Status类型返回值
             */
            public Status status() {
                Status status = new Status();
                status.setLevel(StatusLevel.OK);
                status.setStatus("Test");
                return status;
            }

            @Override
            /**
             * componentName方法。
             * @return String类型返回值
             */
            public String componentName() {
                return "TestComponent";
            }

            @Override
            /**
             * alarm方法。
             */
            public void alarm() {
                // do nothing
            }
        };

        assertNotNull(monitorable.status());
        assertEquals("TestComponent", monitorable.componentName());
    }
}
