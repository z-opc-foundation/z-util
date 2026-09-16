package com.zifang.util.monitor.thread;

import com.zifang.util.monitor.thread.utility.TimeUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池状态。
 *
 * @author zifang
 */
public class ThreadPoolStatus extends Status {

    /**
     * 线程池启动时间。
     */
    protected long startTime = TimeUtil.getMillisTimestamp();

    /**
     * 提交任务数。
     */
    protected AtomicInteger submitCount = new AtomicInteger();

    /**
     * 启动任务数。
     */
    protected AtomicInteger startCount = new AtomicInteger();

    /**
     * 成功执行任务数。
     */
    protected AtomicInteger sucessCount = new AtomicInteger();

    /**
     * 失败任务数。
     */
    protected AtomicInteger failCount = new AtomicInteger();

    /**
     * 上次启动时间。
     */
    protected AtomicLong lastStartTime = new AtomicLong();

    /**
     * 上次结束时间。
     */
    protected AtomicLong lastFinishTime = new AtomicLong();

    /**
     * 总执行时间。
     */
    protected AtomicLong totalTimeConsuming = new AtomicLong();

    /**
     * 告警次数。
     */
    protected AtomicInteger alarmTimes = new AtomicInteger();

    /**
     * 每一个线程的状态。
     */
    protected ConcurrentHashMap<Thread, Map<String, Object>> threadStatusMap = new ConcurrentHashMap<>();

    /**
     * 正在执行的任务状态。
     */
    protected Map<Runnable, Map<String, Object>> taskStatusMap = new ConcurrentHashMap<>();

    /**
     * 默认10秒监测一次。
     */
    protected long monitorInterval = 10 * TimeUtil.SECOND_MILLISECONDS;

    /**
     * 获取线程池启动时间。
     *
     * @return 线程池启动时间（毫秒时间戳）
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 设置线程池启动时间。
     *
     * @param startTime 线程池启动时间（毫秒时间戳）
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取提交任务计数。
     *
     * @return 提交任务计数（原子Integer类型）
     */
    public AtomicInteger getSubmitCount() {
        return submitCount;
    }

    /**
     * 设置提交任务计数。
     *
     * @param submitCount 提交任务计数对象
     */
    public void setSubmitCount(AtomicInteger submitCount) {
        this.submitCount = submitCount;
    }

    /**
     * 获取启动任务计数。
     *
     * @return 启动任务计数（原子Integer类型）
     */
    public AtomicInteger getStartCount() {
        return startCount;
    }

    /**
     * 设置启动任务计数。
     *
     * @param startCount 启动任务计数对象
     */
    public void setStartCount(AtomicInteger startCount) {
        this.startCount = startCount;
    }

    /**
     * 获取成功任务计数。
     *
     * @return 成功任务计数（原子Integer类型）
     */
    public AtomicInteger getSucessCount() {
        return sucessCount;
    }

    /**
     * 设置成功任务计数。
     *
     * @param sucessCount 成功任务计数对象
     */
    public void setSucessCount(AtomicInteger sucessCount) {
        this.sucessCount = sucessCount;
    }

    /**
     * 获取失败任务计数。
     *
     * @return 失败任务计数（原子Integer类型）
     */
    public AtomicInteger getFailCount() {
        return failCount;
    }

    /**
     * 设置失败任务计数。
     *
     * @param failCount 失败任务计数对象
     */
    public void setFailCount(AtomicInteger failCount) {
        this.failCount = failCount;
    }

    /**
     * 获取上次任务启动时间。
     *
     * @return 上次任务启动时间（原子Long类型）
     */
    public AtomicLong getLastStartTime() {
        return lastStartTime;
    }

    /**
     * 设置上次任务启动时间。
     *
     * @param lastStartTime 上次任务启动时间（原子Long类型）
     */
    public void setLastStartTime(AtomicLong lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    /**
     * 获取上次任务结束时间。
     *
     * @return 上次任务结束时间（原子Long类型）
     */
    public AtomicLong getLastFinishTime() {
        return lastFinishTime;
    }

    /**
     * 设置上次任务结束时间。
     *
     * @param lastFinishTime 上次任务结束时间（原子Long类型）
     */
    public void setLastFinishTime(AtomicLong lastFinishTime) {
        this.lastFinishTime = lastFinishTime;
    }

    /**
     * 获取总执行时间。
     *
     * @return 总执行时间（原子Long类型）
     */
    public AtomicLong getTotalTimeConsuming() {
        return totalTimeConsuming;
    }

    /**
     * 设置总执行时间。
     *
     * @param totalTimeConsuming 总执行时间（原子Long类型）
     */
    public void setTotalTimeConsuming(AtomicLong totalTimeConsuming) {
        this.totalTimeConsuming = totalTimeConsuming;
    }

    /**
     * 获取告警次数。
     *
     * @return 告警次数（原子Integer类型）
     */
    public AtomicInteger getAlarmTimes() {
        return alarmTimes;
    }

    /**
     * 设置告警次数。
     *
     * @param alarmTimes 告警次数（原子Integer类型）
     */
    public void setAlarmTimes(AtomicInteger alarmTimes) {
        this.alarmTimes = alarmTimes;
    }

    /**
     * 获取线程状态映射表。
     *
     * @return 线程状态映射表
     */
    public ConcurrentHashMap<Thread, Map<String, Object>> getThreadStatusMap() {
        return threadStatusMap;
    }

    /**
     * 设置线程状态映射表。
     *
     * @param threadStatusMap 线程状态映射表
     */
    public void setThreadStatusMap(ConcurrentHashMap<Thread, Map<String, Object>> threadStatusMap) {
        this.threadStatusMap = threadStatusMap;
    }

    /**
     * 获取任务状态映射表。
     *
     * @return 任务状态映射表
     */
    public Map<Runnable, Map<String, Object>> getTaskStatusMap() {
        return taskStatusMap;
    }

    /**
     * 设置任务状态映射表。
     *
     * @param taskStatusMap 任务状态映射表
     */
    public void setTaskStatusMap(Map<Runnable, Map<String, Object>> taskStatusMap) {
        this.taskStatusMap = taskStatusMap;
    }

    /**
     * 获取监控间隔时间。
     *
     * @return 监控间隔时间（毫秒）
     */
    public long getMonitorInterval() {
        return monitorInterval;
    }

    /**
     * 设置监控间隔时间。
     *
     * @param monitorInterval 监控间隔时间（毫秒）
     */
    public void setMonitorInterval(long monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

    /**
     * 返回线程池状态的字符串表示。
     *
     * @return 包含所有状态字段的字符串表示
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ThreadPoolStatus{" +
                "startTime=" + startTime +
                ", submitCount=" + submitCount +
                ", startCount=" + startCount +
                ", sucessCount=" + sucessCount +
                ", failCount=" + failCount +
                ", lastStartTime=" + lastStartTime +
                ", lastFinishTime=" + lastFinishTime +
                ", totalTimeConsuming=" + totalTimeConsuming +
                ", alarmTimes=" + alarmTimes +
                ", threadStatusMap=" + threadStatusMap +
                ", taskStatusMap=" + taskStatusMap +
                ", monitorInterval=" + monitorInterval +
                '}';
    }
}
