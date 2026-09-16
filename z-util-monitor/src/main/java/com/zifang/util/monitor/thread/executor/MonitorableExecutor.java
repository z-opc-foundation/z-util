package com.zifang.util.monitor.thread.executor;

import com.zifang.util.monitor.thread.Monitorable;
import com.zifang.util.monitor.thread.alarm.Alarmable;


/**
 * 可监控的执行器接口。
 * <p>
 * 继承自Monitorable和Alarmable接口，
 * 用于标识可以监控和告警的线程池执行器。
 *
 * @author zifang
 * @see Monitorable
 * @see Alarmable
 */
public interface MonitorableExecutor extends Monitorable, Alarmable {
}
