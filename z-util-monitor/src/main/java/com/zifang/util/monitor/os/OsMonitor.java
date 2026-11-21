package com.zifang.util.monitor.os;

import com.sun.management.OperatingSystemMXBean;
import com.zifang.util.monitor.platform.Platform;
import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.SampleSink;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 宿主机监控采集器。
 * <p>
 * 采集操作系统层面的信息：CPU 核数与负载、物理内存与交换区、宿主机运行时长、
 * 进程 CPU 时间、文件描述符占用。
 * </p>
 * <p>
 * 分工 —— JVM 能自己拿到的读数（CPU、物理内存、交换区、进程 CPU 时间）走
 * {@code com.sun.management} 扩展 bean，天然跨平台；「必须问操作系统」的读数
 * （开机时长、文件描述符）委托 {@link Platform#host()} 平台探针，Linux 读
 * {@code /proc}，macOS 走 {@code UnixOperatingSystemMXBean} + {@code sysctl}，
 * 其余平台探针返回 -1，对应指标自动不产出。
 * </p>
 *
 * @author zifang
 */
public class OsMonitor implements MetricsSampler {

    private final java.lang.management.OperatingSystemMXBean osMXBean;
    private final OperatingSystemMXBean sunOsMXBean;
    private final List<MetricDefinition> definitions;

    /**
     * OsMonitor方法。
     */
    public OsMonitor() {
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.sunOsMXBean = resolveSunOsMXBean();
        this.definitions = buildDefinitions();
    }

    private static OperatingSystemMXBean resolveSunOsMXBean() {
        try {
            java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            return bean instanceof OperatingSystemMXBean ? (OperatingSystemMXBean) bean : null;
        } catch (Throwable e) {
            return null;
        }
    }

    private static MetricDefinition def(String name, String description, String unit, MetricDefinition.Type type) {
        return new MetricDefinition(name, MetricDefinition.Category.HOST, unit, type, description);
    }

    private List<MetricDefinition> buildDefinitions() {
        List<MetricDefinition> list = new ArrayList<>();
        list.add(def("host.cpu.cores", "可用处理器核数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("host.cpu.load", "CPU 使用率", "percent", MetricDefinition.Type.GAUGE));
        list.add(def("host.cpu.system.load.average", "系统平均负载", "count", MetricDefinition.Type.GAUGE));
        list.add(def("host.memory.total", "物理内存总量", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("host.memory.free", "物理内存可用量", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("host.memory.used", "物理内存已用量", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("host.memory.used.percent", "物理内存使用率", "percent", MetricDefinition.Type.GAUGE));
        list.add(def("host.swap.total", "交换区总量", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("host.swap.free", "交换区可用量", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("host.uptime", "宿主机运行时长", "ms", MetricDefinition.Type.GAUGE));
        list.add(def("host.process.pid", "当前进程 PID", "count", MetricDefinition.Type.GAUGE));
        list.add(def("host.process.cpu.time", "进程累计 CPU 时间", "ns", MetricDefinition.Type.COUNTER));
        list.add(def("host.file.descriptors", "已打开文件描述符数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("host.file.descriptors.limit", "文件描述符上限", "count", MetricDefinition.Type.GAUGE));
        return list;
    }

    @Override
    public String id() {
        return "host";
    }

    @Override
    public Collection<MetricDefinition> definitions() {
        return Collections.unmodifiableList(definitions);
    }

    @Override
    public void sample(SampleSink sink) {
        sink.put("host.cpu.cores", osMXBean.getAvailableProcessors());
        double systemLoad = osMXBean.getSystemLoadAverage();
        if (systemLoad >= 0) {
            sink.put("host.cpu.system.load.average", systemLoad);
        }

        if (sunOsMXBean != null) {
            double cpuLoad = sunOsMXBean.getCpuLoad();
            if (cpuLoad >= 0) {
                sink.put("host.cpu.load", cpuLoad * 100.0);
            }
            long total = sunOsMXBean.getTotalPhysicalMemorySize();
            long free = sunOsMXBean.getFreePhysicalMemorySize();
            if (total > 0) {
                sink.put("host.memory.total", total);
                sink.put("host.memory.free", free);
                sink.put("host.memory.used", total - free);
                sink.put("host.memory.used.percent", (total - free) * 100.0 / total);
            }
            long swapTotal = sunOsMXBean.getTotalSwapSpaceSize();
            if (swapTotal > 0) {
                sink.put("host.swap.total", swapTotal);
                sink.put("host.swap.free", sunOsMXBean.getFreeSwapSpaceSize());
            }
            long processCpu = sunOsMXBean.getProcessCpuTime();
            if (processCpu > 0) {
                sink.put("host.process.cpu.time", processCpu);
            }
        }

        long uptime = Platform.host().uptimeMillis();
        if (uptime > 0) {
            sink.put("host.uptime", uptime);
        }
        sink.put("host.process.pid", ProcessHandle.current().pid());

        int descriptors = Platform.host().openFileDescriptorCount();
        if (descriptors >= 0) {
            sink.put("host.file.descriptors", descriptors);
        }
        long limit = Platform.host().fileDescriptorLimit();
        if (limit > 0) {
            sink.put("host.file.descriptors.limit", limit);
        }

        sink.attribute("host.name", osMXBean.getName());
        sink.attribute("host.os.name", osMXBean.getName());
        sink.attribute("host.os.arch", osMXBean.getArch());
        sink.attribute("host.os.version", osMXBean.getVersion());
        sink.attribute("host.user.dir", System.getProperty("user.dir"));
        sink.attribute("host.java.version", System.getProperty("java.version"));
    }
}
