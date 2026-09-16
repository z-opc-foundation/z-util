package com.zifang.util.monitor.thread.executor;

import com.zifang.util.core.lang.concurrency.NameThreadFactory;
import com.zifang.util.monitor.thread.MonitorManager;
import com.zifang.util.monitor.thread.Monitorable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 线程池管理器。
 *
 * @author zifang
 */
public class ExecutorManager {

    /**
     * 获取任务类型的掩码。二进制位11111111。
     */
    public static final byte TASK_EXECUTE_TYPE_MASK = 0xF;
    /**
     * CPU密集型任务。二进制位00000001。
     */
    public static final byte CPU_INTENSIVE = 0x1;
    /**
     * IO密集型任务。二进制位00000010。
     */
    public static final byte IO_INTENSIVE = 0x2;
    /**
     * 日志对象。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorManager.class);
    /**
     * 监控组件管理器。
     */
    private final MonitorManager monitorManager;
    /**
     * 每个任务对应一个单工作线程线程池。
     */
    private ConcurrentHashMap<String, ExecutorService> threadPoolMap;
    /**
     * 线程池配置列表。
     */
    private List<ThreadPoolConfigUnit> threadPoolConfigUnitList;

    /**
     * 构造函数。
     *
     * @param threadPoolConfigUnitList 需要管理的线程池配置列表。
     */
    public ExecutorManager(List<ThreadPoolConfigUnit> threadPoolConfigUnitList) {
        monitorManager = new MonitorManager();
        this.threadPoolConfigUnitList = threadPoolConfigUnitList;
        if (this.threadPoolConfigUnitList == null || this.threadPoolConfigUnitList.size() <= 0) {
            return;
        }
        //开始给内置的ThreadPoolExecutor做初始化
        threadPoolMap = new ConcurrentHashMap<>(this.threadPoolConfigUnitList.size());
        Map<Long, List<Monitorable>> monitorAggregation = new HashMap<>(this.threadPoolConfigUnitList.size());
        for (ThreadPoolConfigUnit threadPoolConfigUnit : this.threadPoolConfigUnitList) {
            //获取并设置线程池大小。
            generateThreadPoolSizeFromConfig(threadPoolConfigUnit);
            /**
             *  每种任务分配一个线程池。创建一个固定容量的线程池，{@link java.util.concurrent.Executors#newFixedThreadPool}
             */
            FixedMonitorableExecutor threadPoolExecutor = new FixedMonitorableExecutor(
                    threadPoolConfigUnit,
                    new LinkedBlockingQueue<Runnable>(),
                    new NameThreadFactory().setNameFormat(threadPoolConfigUnit.getPoolName() + "-%d").build());
            //将线程池保存起来。
            threadPoolMap.put(threadPoolConfigUnit.getPoolName(), threadPoolExecutor);
            //将每个监控周期的被监控组件组合成一个map
            if (monitorAggregation.get(threadPoolConfigUnit.getMonitorInterval()) == null) {
                monitorAggregation.put(threadPoolConfigUnit.getMonitorInterval(),
                        new ArrayList<Monitorable>(Arrays.asList(threadPoolExecutor)));
            } else {
                monitorAggregation.get(threadPoolConfigUnit.getMonitorInterval()).add(threadPoolExecutor);
            }
        }
        //把每个线程池加入到监控列表中。
        monitorManager.addAllMonitor(monitorAggregation);
    }

    /**
     * 根据线程池类型，获取线程池大小。
     * cpu密集型任务的线程池容量为CPU核数+1，IO密集型任务的线程池容量为2 * CPU核数。
     *
     * @param poolType 线程池类型。
     * @return 线程池大小。
     */
    private int getThreadPoolSizeFromComputeType(int poolType) {
        switch (poolType & TASK_EXECUTE_TYPE_MASK) {
            case CPU_INTENSIVE:
                return Runtime.getRuntime().availableProcessors() + 1;
            case IO_INTENSIVE:
                return 2 * Runtime.getRuntime().availableProcessors();
            default:
                return 2 * Runtime.getRuntime().availableProcessors();
        }
    }

    /**
     * 根据配置单元，生成线程池容量。
     *
     * @param threadPoolConfigUnit 线程池配置单元。
     */
    private void generateThreadPoolSizeFromConfig(ThreadPoolConfigUnit threadPoolConfigUnit) {
        if (threadPoolConfigUnit.getPoolSize() > 0) {
            return;
        }
        if (threadPoolConfigUnit.getComputeType() > 0) {
            threadPoolConfigUnit.setPoolSize(getThreadPoolSizeFromComputeType(threadPoolConfigUnit.getComputeType()));
        }
        threadPoolConfigUnit.setPoolSize(getThreadPoolSizeFromComputeType(IO_INTENSIVE));
    }

    /**
     * 获取处理缓存键任务的线程池。
     *
     * @param poolName 线程池名称。
     * @return 获取缓存键任务执行线程池。
     */
    public ExecutorService getExecutor(String poolName) {
        return threadPoolMap.get(poolName);
    }

    /**
     * 获取监控管理器。
     *
     * @return 监控管理器。
     */
    public MonitorManager getMonitorManager() {
        return monitorManager;
    }
}
