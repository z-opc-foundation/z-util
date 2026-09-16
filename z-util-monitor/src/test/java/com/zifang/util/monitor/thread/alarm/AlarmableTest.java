package com.zifang.util.monitor.thread.alarm;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * AlarmableTest类。
 */
public class AlarmableTest {

    @Test
    /**
     * testAlarmableInterface方法。
     */
    public void testAlarmableInterface() {
        Alarmable alarmable = new Alarmable() {
            @Override
            /**
             * alarm方法。
             */
            public void alarm() {
                // do nothing
            }
        };
        assertNotNull(alarmable);
    }
}
