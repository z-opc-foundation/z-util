package com.zifang.util.monitor.thread.alarm;

import com.zifang.util.monitor.thread.Status;

/**
 * 告警策略抽象基类。
 * <p>
 * 定义判断监控组件是否需要告警的策略接口，
 * 子类实现具体的告警判断逻辑。
 *
 * @author zifang
 * @see Status
 */
public abstract class AlarmPolicy {

    /**
     * 根据传入的status，决定是否需要告警。
     *
     * @param status 待判断的状态体，包含监控组件的当前状态信息
     * @return 如果需要告警，返回true，否则返回false
     */
    public abstract boolean needAlarm(Status status);
}
