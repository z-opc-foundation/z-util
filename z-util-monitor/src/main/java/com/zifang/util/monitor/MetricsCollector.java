package com.zifang.util.monitor;

import java.lang.management.*;
import java.util.List;

/**
 * 内置指标采集器
 * <p>
 * 提供 JVM、操作系统、线程等系统指标的采集功能。
 * 可通过 {@link MetricsRegistry} 进行注册，支持按需启用/停用。
 * </p>
 */
public class MetricsCollector {

    private final MetricsRegistry registry;
    // MXBeans
    private final MemoryMXBean memoryMXBean;
    private final ThreadMXBean threadMXBean;
    private final OperatingSystemMXBean osMXBean;
    private final RuntimeMXBean runtimeMXBean;
    private final ClassLoadingMXBean classLoadingMXBean;
    private final List<GarbageCollectorMXBean> gcMXBeans;
    private boolean jvmEnabled = false;
    private boolean osEnabled = false;
    private boolean threadEnabled = false;

    /**
     * MetricsCollector方法。
     */
    public MetricsCollector() {
        this.registry = MetricsRegistry.getInstance();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        this.gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }

    /**
     * 启用所有内置指标采集
     */
    public void enableAll() {
        enableJvm();
        enableOs();
        enableThread();
    }

    /**
     * 禁用所有内置指标采集
     */
    public void disableAll() {
        disableJvm();
        disableOs();
        disableThread();
        registry.setEnabled(false);
    }

    /**
     * 启用 JVM 指标采集
     */
    public void enableJvm() {
        if (jvmEnabled) {
            return;
        }
        jvmEnabled = true;

        // 堆内存
        registry.register("jvm.memory.heap.used", MetricsSnapshot.Category.JVM,
                () -> memoryMXBean.getHeapMemoryUsage().getUsed(), "堆内存已使用", "bytes");
        registry.register("jvm.memory.heap.committed", MetricsSnapshot.Category.JVM,
                () -> memoryMXBean.getHeapMemoryUsage().getCommitted(), "堆内存已提交", "bytes");
        registry.register("jvm.memory.heap.max", MetricsSnapshot.Category.JVM,
                () -> memoryMXBean.getHeapMemoryUsage().getMax(), "堆内存最大", "bytes");
        registry.register("jvm.memory.heap.init", MetricsSnapshot.Category.JVM,
                () -> memoryMXBean.getHeapMemoryUsage().getInit(), "堆内存初始", "bytes");

        // 非堆内存
        registry.register("jvm.memory.nonheap.used", MetricsSnapshot.Category.JVM,
                () -> memoryMXBean.getNonHeapMemoryUsage().getUsed(), "非堆内存已使用", "bytes");
        registry.register("jvm.memory.nonheap.committed", MetricsSnapshot.Category.JVM,
                () -> memoryMXBean.getNonHeapMemoryUsage().getCommitted(), "非堆内存已提交", "bytes");
        registry.register("jvm.memory.nonheap.max", MetricsSnapshot.Category.JVM,
                () -> memoryMXBean.getNonHeapMemoryUsage().getMax(), "非堆内存最大", "bytes");

        // 运行时信息
        registry.register("jvm.runtime.uptime", MetricsSnapshot.Category.JVM,
                () -> runtimeMXBean.getUptime(), "JVM运行时长", "ms");
        registry.register("jvm.runtime.name", MetricsSnapshot.Category.JVM,
                runtimeMXBean::getVmName, "JVM名称");
        registry.register("jvm.runtime.vendor", MetricsSnapshot.Category.JVM,
                runtimeMXBean::getVmVendor, "JVM厂商");
        registry.register("jvm.runtime.version", MetricsSnapshot.Category.JVM,
                runtimeMXBean::getVmVersion, "JVM版本");
        registry.register("jvm.runtime.inputargs", MetricsSnapshot.Category.JVM,
                () -> String.join(" ", runtimeMXBean.getInputArguments()), "JVM启动参数");

        // 类加载
        registry.register("jvm.class.loaded", MetricsSnapshot.Category.JVM,
                () -> classLoadingMXBean.getLoadedClassCount(), "已加载类数量");
        registry.register("jvm.class.unloaded", MetricsSnapshot.Category.JVM,
                () -> classLoadingMXBean.getUnloadedClassCount(), "已卸载类数量");
        registry.register("jvm.class.total", MetricsSnapshot.Category.JVM,
                () -> classLoadingMXBean.getTotalLoadedClassCount(), "累计加载类数量");

        // GC
        for (int i = 0; i < gcMXBeans.size(); i++) {
            final int idx = i;
            GarbageCollectorMXBean gc = gcMXBeans.get(i);
            String name = gc.getName().replaceAll("[^a-zA-Z0-9]", "_");
            registry.register("jvm.gc." + name + ".count", MetricsSnapshot.Category.JVM,
                    gc::getCollectionCount, "GC次数");
            registry.register("jvm.gc." + name + ".time", MetricsSnapshot.Category.JVM,
                    gc::getCollectionTime, "GC耗时", "ms");
        }

        registry.setEnabled(true);
    }

    /**
     * 停用 JVM 指标采集
     */
    public void disableJvm() {
        if (!jvmEnabled) {
            return;
        }
        jvmEnabled = false;

        // 移除 JVM 相关指标
        registry.getMetricNames(MetricsSnapshot.Category.JVM).forEach(registry::unregister);

        // 如果没有其他指标启用，禁用 registry
        if (!osEnabled && !threadEnabled && registry.getMetricNames(MetricsSnapshot.Category.CUSTOM).isEmpty()) {
            registry.setEnabled(false);
        }
    }

    /**
     * 启用操作系统指标采集
     */
    public void enableOs() {
        if (osEnabled) {
            return;
        }
        osEnabled = true;

        registry.register("os.name", MetricsSnapshot.Category.OS,
                () -> osMXBean.getName(), "操作系统名称");
        registry.register("os.arch", MetricsSnapshot.Category.OS,
                () -> osMXBean.getArch(), "系统架构");
        registry.register("os.version", MetricsSnapshot.Category.OS,
                () -> osMXBean.getVersion(), "操作系统版本");
        registry.register("os.availableprocessors", MetricsSnapshot.Category.OS,
                () -> osMXBean.getAvailableProcessors(), "可用处理器数量");
        registry.register("os.systemloadaverage", MetricsSnapshot.Category.OS,
                () -> osMXBean.getSystemLoadAverage(), "系统平均负载");

        // 系统内存 (JVM 最大堆内存)
        registry.register("os.memory.total", MetricsSnapshot.Category.OS,
                () -> Runtime.getRuntime().maxMemory(), "系统总内存", "bytes");

        registry.setEnabled(true);
    }

    /**
     * 停用操作系统指标采集
     */
    public void disableOs() {
        if (!osEnabled) {
            return;
        }
        osEnabled = false;

        registry.getMetricNames(MetricsSnapshot.Category.OS).forEach(registry::unregister);

        if (!jvmEnabled && !threadEnabled && registry.getMetricNames(MetricsSnapshot.Category.CUSTOM).isEmpty()) {
            registry.setEnabled(false);
        }
    }

    /**
     * 启用线程指标采集
     */
    public void enableThread() {
        if (threadEnabled) {
            return;
        }
        threadEnabled = true;

        registry.register("jvm.thread.count", MetricsSnapshot.Category.THREAD,
                () -> threadMXBean.getThreadCount(), "当前线程数量");
        registry.register("jvm.thread.peak", MetricsSnapshot.Category.THREAD,
                () -> threadMXBean.getPeakThreadCount(), "峰值线程数量");
        registry.register("jvm.thread.daemon", MetricsSnapshot.Category.THREAD,
                () -> threadMXBean.getDaemonThreadCount(), "守护线程数量");
        registry.register("jvm.thread.totalstarted", MetricsSnapshot.Category.THREAD,
                () -> threadMXBean.getTotalStartedThreadCount(), "累计启动线程数量");

        // 线程状态分布
        registry.register("jvm.thread.states.runnable", MetricsSnapshot.Category.THREAD,
                () -> {
                    ThreadInfo[] infos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
                    long count = 0;
                    for (ThreadInfo info : infos) {
                        if (info != null && info.getThreadState() == Thread.State.RUNNABLE) {
                            count++;
                        }
                    }
                    return count;
                }, "RUNNABLE 线程数量");
        registry.register("jvm.thread.states.waiting", MetricsSnapshot.Category.THREAD,
                () -> {
                    ThreadInfo[] infos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
                    long count = 0;
                    for (ThreadInfo info : infos) {
                        if (info != null && info.getThreadState() == Thread.State.WAITING) {
                            count++;
                        }
                    }
                    return count;
                }, "WAITING 线程数量");
        registry.register("jvm.thread.states.blocked", MetricsSnapshot.Category.THREAD,
                () -> {
                    ThreadInfo[] infos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds());
                    long count = 0;
                    for (ThreadInfo info : infos) {
                        if (info != null && info.getThreadState() == Thread.State.BLOCKED) {
                            count++;
                        }
                    }
                    return count;
                }, "BLOCKED 线程数量");

        registry.setEnabled(true);
    }

    /**
     * 停用线程指标采集
     */
    public void disableThread() {
        if (!threadEnabled) {
            return;
        }
        threadEnabled = false;

        registry.getMetricNames(MetricsSnapshot.Category.THREAD).forEach(registry::unregister);

        if (!jvmEnabled && !osEnabled && registry.getMetricNames(MetricsSnapshot.Category.CUSTOM).isEmpty()) {
            registry.setEnabled(false);
        }
    }

    /**
     * isJvmEnabled方法。
     *
     * @return boolean类型返回值
     */
    public boolean isJvmEnabled() {
        return jvmEnabled;
    }

    /**
     * isOsEnabled方法。
     *
     * @return boolean类型返回值
     */
    public boolean isOsEnabled() {
        return osEnabled;
    }

    /**
     * isThreadEnabled方法。
     *
     * @return boolean类型返回值
     */
    public boolean isThreadEnabled() {
        return threadEnabled;
    }

    /**
     * getRegistry方法。
     *
     * @return MetricsRegistry类型返回值
     */
    public MetricsRegistry getRegistry() {
        return registry;
    }
}
