package com.zifang.util.monitor.thread.alarm;

/**
 * 告警接口，实现此接口的组件均需实现自身的告警逻辑。
 *
 * @author zifang
 */
public interface Alarmable {

    /**
     * 告警接口。
     */
    void alarm();
}
