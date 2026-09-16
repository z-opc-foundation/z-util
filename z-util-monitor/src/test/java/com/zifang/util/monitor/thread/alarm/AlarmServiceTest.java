package com.zifang.util.monitor.thread.alarm;

import org.junit.Test;

/**
 * AlarmServiceTest类。
 */
public class AlarmServiceTest {

    @Test
    /**
     * testLogAlarmServiceAlarm方法。
     */
    public void testLogAlarmServiceAlarm() {
        AlarmService alarmService = new LogAlarmService();
        // Should not throw exception
        alarmService.alarm("test message");
        alarmService.alarm();
        alarmService.alarm("arg1", "arg2", "arg3");
    }

    @Test
    /**
     * testLogAlarmServiceAlarmWithNull方法。
     */
    public void testLogAlarmServiceAlarmWithNull() {
        AlarmService alarmService = new LogAlarmService();
        // Should not throw exception
        alarmService.alarm((Object[]) null);
    }
}
