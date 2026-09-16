package com.zifang.util.monitor.thread.alarm;

import com.zifang.util.monitor.thread.Status;
import com.zifang.util.monitor.thread.ThreadPoolStatus;
import com.zifang.util.monitor.thread.utility.TimeUtil;

/**
 * 线程池任务超时告警策略。当最后一个启动的任务，超过alarmThreshold没有结束时，此策略认为该监控组件状态异常，需告警。
 *
 * @author zifang
 */
public class ThreadPoolOvertimeAlarmPolicy extends AlarmPolicy {

    /**
     * 告警阀值，单位为毫秒。
     */
    private int alarmThreshold = 2 * TimeUtil.MINUTES_SECONDS * TimeUtil.SECOND_MILLISECONDS;

    /**
     * 默认构造函数，使用默认告警阈值（2分钟）。
     */
    public ThreadPoolOvertimeAlarmPolicy() {
    }

    /**
     * 构造函数，指定告警阈值。
     *
     * @param alarmThreshold 告警阈值，单位为毫秒
     */
    public ThreadPoolOvertimeAlarmPolicy(int alarmThreshold) {
        this.alarmThreshold = alarmThreshold;
    }

    @Override
    /**
     * needAlarm方法。
     *      * @param status Status类型参数
     * @return boolean类型返回值
     */
    public boolean needAlarm(Status status) {
        ThreadPoolStatus threadPoolStatus = (ThreadPoolStatus) status;
        //当最后一个启动的任务超过10分钟没有结束时，该线程池已经异常。
        return threadPoolStatus.getLastFinishTime().get() < threadPoolStatus.getLastStartTime().get()
                && (TimeUtil.getMillisTimestamp() - threadPoolStatus.getLastStartTime().get()) >= alarmThreshold;
    }
}
