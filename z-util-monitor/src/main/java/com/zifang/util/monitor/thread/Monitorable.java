package com.zifang.util.monitor.thread;

import com.zifang.util.monitor.thread.alarm.Alarmable;

/**
 * 可监控组件的接口。
 * <p>
 * 实现此接口的组件可以被监控管理器定期采集状态信息，
 * 当状态异常时自动触发告警。
 *
 * @author zifang
 * @see Alarmable
 */
public interface Monitorable extends Alarmable {

    /**
     * 获取该监控组件的状态。
     *
     * @return 状态对象，包含组件的运行状态和级别信息
     */
    Status status();

    /**
     * 获取组件名称。
     *
     * @return 该组件的名称，用于在监控系统中标识
     */
    String componentName();
}
