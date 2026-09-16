package com.zifang.util.monitor.thread.constant;

/**
 * 监控模块相关常量。
 *
 * @author zifang
 */
public final class MonitorConstant {

    /**
     * 初始状态，可将各个组件的初始状态值设置成此对象。
     */
    public static final Object INIT_STATUS = new Object();
    /**
     * 线程开始时间键。
     */
    public static final String THREAD_START_TIME = "threadStartTime";
    /**
     * 任务开始时间键。
     */
    public static final String TASK_START_TIME = "taskStartTime";

    /**
     * 不允许创建常量类。
     */
    private MonitorConstant() {
    }
}
