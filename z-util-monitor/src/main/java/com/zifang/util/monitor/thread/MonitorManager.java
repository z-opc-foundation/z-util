package com.zifang.util.monitor.thread;

import com.zifang.util.core.lang.concurrency.NameThreadFactory;
import com.zifang.util.monitor.thread.task.MonitorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 监控组件管理器。
 *
 * @author zifang
 */
public class MonitorManager {

    /**
     * 日志对象。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManager.class);

    /**
     * 监控组件集合。
     */
    private Map<Long, List<Monitorable>> monitorAggregation;

    /**
     * 执行监控任务的定时线程池列表。
     */
    private List<ScheduledExecutorService> scheduledExecutorServiceList;

    /**
     * 构造函数。
     */
    public MonitorManager() {
        scheduledExecutorServiceList = new ArrayList<>();
        monitorAggregation = new HashMap<>();
    }

    /**
     * 把被监控组件集合全部加入到监控集合中。
     *
     * @param monitorAggregation 待增加的监控组件集合。
     */
    public synchronized void addAllMonitor(Map<Long, List<Monitorable>> monitorAggregation) {
        //运行期不支持新增
        if (this.monitorAggregation != null && this.monitorAggregation.size() > 0) {
            throw new IllegalStateException("Not supported function addAllMonitor in run-time.");
        }
        //为每个监控周期新建一个定时任务线程池。
        for (Map.Entry<Long, List<Monitorable>> entry : monitorAggregation.entrySet()) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                    new NameThreadFactory().setNameFormat("monitor-thread-%d").build());
            scheduledExecutorService.scheduleAtFixedRate(new MonitorTask(entry.getValue()),
                    entry.getKey(), entry.getKey(), TimeUnit.MILLISECONDS);
            scheduledExecutorServiceList.add(scheduledExecutorService);
            if (this.monitorAggregation.get(entry.getKey()) == null) {
                this.monitorAggregation.put(entry.getKey(), entry.getValue());
            } else {
                this.monitorAggregation.get(entry.getKey()).addAll(entry.getValue());
            }
        }
    }

    /**
     * 关闭监控线程池。
     *
     * @param forceShutdown 是否强制关闭。
     */
    public void shutdown(boolean forceShutdown) {
        for (ScheduledExecutorService scheduledExecutorService : scheduledExecutorServiceList) {
            if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
                if (!forceShutdown) {
                    scheduledExecutorService.shutdown();
                } else {
                    scheduledExecutorService.shutdownNow();
                }
            }
        }
    }
}
