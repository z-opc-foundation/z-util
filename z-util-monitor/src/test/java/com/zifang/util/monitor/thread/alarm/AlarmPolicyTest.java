package com.zifang.util.monitor.thread.alarm;

import com.zifang.util.monitor.thread.Status;
import com.zifang.util.monitor.thread.StatusLevel;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * AlarmPolicyTest类。
 */
public class AlarmPolicyTest {

    @Test
    /**
     * testAlarmPolicyAbstract方法。
     */
    public void testAlarmPolicyAbstract() {
        // Test concrete implementation
        AlarmPolicy policy = new AlarmPolicy() {
            @Override
            /**
             * needAlarm方法。
             *      * @param status Status类型参数
             * @return boolean类型返回值
             */
            public boolean needAlarm(Status status) {
                return status.getLevel() == StatusLevel.ERROR;
            }
        };

        Status okStatus = new Status();
        okStatus.setLevel(StatusLevel.OK);
        assertFalse(policy.needAlarm(okStatus));

        Status errorStatus = new Status();
        errorStatus.setLevel(StatusLevel.ERROR);
        assertTrue(policy.needAlarm(errorStatus));
    }
}
