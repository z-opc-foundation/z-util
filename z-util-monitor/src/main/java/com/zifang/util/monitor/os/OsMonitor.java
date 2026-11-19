package com.zifang.util.monitor.os;

import com.sun.management.OperatingSystemMXBean;
import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.SampleSink;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 宿主机监控采集器。
 * <p>
 * 采集操作系统层面的信息：CPU 核数与负载、物理内存与交换区、宿主机运行时长、
 * 进程 CPU 时间、文件描述符占用。跨平台实现 —— 有 {@code com.sun.management}
 * 扩展 bean 的读数走扩展接口，宿主机运行时长与文件描述符在 Linux 下读
 * {@code /proc}，其它平台自动降级为不产出该指标。
 * </p>
 *
 * @author zifang
 */
public class OsMonitor implements MetricsSampler {

    private static final Path PROC_UPTIME = Paths.get("/proc/uptime");
    private static final Path PROC_SELF_FD = Paths.get("/proc/self/fd");
    private static final Path PROC_SELF_LIMITS = Paths.get("/proc/self/limits");

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

        long uptime = readHostUptimeMillis();
        if (uptime > 0) {
            sink.put("host.uptime", uptime);
        }
        sink.put("host.process.pid", ProcessHandle.current().pid());

        int descriptors = countOpenFileDescriptors();
        if (descriptors >= 0) {
            sink.put("host.file.descriptors", descriptors);
        }
        long limit = readFileDescriptorLimit();
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

    /**
     * 读取宿主机运行时长。
     *
     * @return long类型返回值，非 Linux 或读取失败时返回 -1
     */
    private static long readHostUptimeMillis() {
        String content = readFirstLine(PROC_UPTIME);
        if (content == null) {
            return -1;
        }
        return parseHostUptimeMillis(content);
    }

    /**
     * 解析 {@code /proc/uptime} 的内容（秒 + 空闲秒）。
     *
     * @param content String类型参数
     *
     * @return long类型返回值，毫秒；解析不了时返回 -1
     */
    static long parseHostUptimeMillis(String content) {
        if (content == null) {
            return -1;
        }
        int space = content.indexOf(' ');
        try {
            double seconds = Double.parseDouble(space > 0 ? content.substring(0, space) : content.trim());
            return (long) (seconds * 1000);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int countOpenFileDescriptors() {
        if (!Files.isDirectory(PROC_SELF_FD)) {
            return -1;
        }
        try (java.util.stream.Stream<Path> stream = Files.list(PROC_SELF_FD)) {
            return (int) stream.count();
        } catch (IOException e) {
            return -1;
        }
    }

    private static long readFileDescriptorLimit() {
        return parseFileDescriptorLimit(readLines(PROC_SELF_LIMITS));
    }

    /**
     * 解析 {@code /proc/self/limits} 里的文件描述符上限。
     *
     * @param lines ListString类型参数
     *
     * @return long类型返回值，读不到时返回 -1
     */
    static long parseFileDescriptorLimit(List<String> lines) {
        for (String line : lines) {
            if (!line.startsWith("Max open files")) {
                continue;
            }
            String[] parts = line.replaceAll("^[^\\d]*", "").trim().split("\\s+");
            if (parts.length > 0) {
                try {
                    return Long.parseLong(parts[0]);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    private static String readFirstLine(Path path) {
        List<String> lines = readLines(path);
        return lines.isEmpty() ? null : lines.get(0);
    }

    private static List<String> readLines(Path path) {
        if (!Files.isReadable(path)) {
            return Collections.emptyList();
        }
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
