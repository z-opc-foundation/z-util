package com.zifang.util.monitor.jvm;

import com.sun.management.OperatingSystemMXBean;
import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.SampleSink;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * JVM 监控采集器。
 * <p>
 * 采集 JVM 运行期的基础面：堆与非堆内存、分代内存池、GC 次数与耗时、类加载、
 * 线程数量与状态分布、即时编译耗时、运行时长与进程 CPU 时间。
 * </p>
 * <p>
 * 所有 MXBean 在构造时一次性取好，采样时只读，避免每次采样都做 bean 查找。
 * </p>
 *
 * @author zifang
 * @see java.lang.management.ManagementFactory
 */
public class JvmMonitor implements MetricsSampler {

    private final MemoryMXBean memoryMXBean;
    private final ThreadMXBean threadMXBean;
    private final RuntimeMXBean runtimeMXBean;
    private final ClassLoadingMXBean classLoadingMXBean;
    private final CompilationMXBean compilationMXBean;
    private final List<GarbageCollectorMXBean> gcMXBeans;
    private final List<MemoryPoolMXBean> poolMXBeans;
    private final List<MetricDefinition> definitions;
    private final OperatingSystemMXBean sunOsMXBean;

    /**
     * 本采集器观察到的最大 GC 停顿。MXBean 只暴露「最近一次」GC，看不到历史峰值，
     * 这个值只能由采集器自己一路盯着攒出来。
     */
    private volatile long maxPauseObserved = -1;

    /**
     * JvmMonitor方法。
     */
    public JvmMonitor() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        this.compilationMXBean = ManagementFactory.getCompilationMXBean();
        this.gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.poolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
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

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private List<MetricDefinition> buildDefinitions() {
        List<MetricDefinition> list = new ArrayList<>();
        list.add(def("jvm.memory.heap.used", "堆内存已使用", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.memory.heap.committed", "堆内存已提交", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.memory.heap.max", "堆内存上限", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.memory.heap.init", "堆内存初始值", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.memory.heap.usage", "堆内存使用率", "percent", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.memory.nonheap.used", "非堆内存已使用", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.memory.nonheap.committed", "非堆内存已提交", "bytes", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.memory.nonheap.max", "非堆内存上限", "bytes", MetricDefinition.Type.GAUGE));

        for (MemoryPoolMXBean pool : poolMXBeans) {
            list.add(def("jvm.memory.pool." + sanitize(pool.getName()) + ".used",
                    "内存池 " + pool.getName() + " 已使用", "bytes", MetricDefinition.Type.GAUGE));
            list.add(def("jvm.memory.pool." + sanitize(pool.getName()) + ".committed",
                    "内存池 " + pool.getName() + " 已提交", "bytes", MetricDefinition.Type.GAUGE));
        }

        for (GarbageCollectorMXBean gc : gcMXBeans) {
            String name = sanitize(gc.getName());
            list.add(def("jvm.gc." + name + ".count", "GC 次数", "count", MetricDefinition.Type.COUNTER));
            list.add(def("jvm.gc." + name + ".time", "GC 累计耗时", "ms", MetricDefinition.Type.COUNTER));
        }
        list.add(def("jvm.gc.pause.last", "最近一次 GC 停顿", "ms", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.gc.pause.max", "历史最大 GC 停顿", "ms", MetricDefinition.Type.GAUGE));

        list.add(def("jvm.class.loaded", "当前已加载类数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.class.unloaded", "已卸载类数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.class.total", "累计加载类数", "count", MetricDefinition.Type.COUNTER));

        list.add(def("jvm.thread.count", "当前线程数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.thread.peak", "峰值线程数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.thread.daemon", "守护线程数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.thread.alive", "存活线程数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.thread.totalstarted", "累计启动线程数", "count", MetricDefinition.Type.COUNTER));
        for (Thread.State state : Thread.State.values()) {
            list.add(def("jvm.thread.state." + state.name().toLowerCase(),
                    state.name() + " 状态线程数", "count", MetricDefinition.Type.GAUGE));
        }

        list.add(def("jvm.compile.total.time", "JIT 编译累计耗时", "ms", MetricDefinition.Type.COUNTER));
        list.add(def("jvm.runtime.uptime", "JVM 运行时长", "ms", MetricDefinition.Type.GAUGE));
        list.add(def("jvm.process.cpu.time", "进程累计 CPU 时间", "ns", MetricDefinition.Type.COUNTER));
        return list;
    }

    private static MetricDefinition def(String name, String description, String unit, MetricDefinition.Type type) {
        return new MetricDefinition(name, MetricDefinition.Category.JVM, unit, type, description);
    }

    @Override
    public String id() {
        return "jvm";
    }

    @Override
    public Collection<MetricDefinition> definitions() {
        return Collections.unmodifiableList(definitions);
    }

    @Override
    public void sample(SampleSink sink) {
        sampleMemory(sink);
        sampleGc(sink);
        sampleClassLoading(sink);
        sampleThreads(sink);
        sampleRuntime(sink);
    }

    private void sampleMemory(SampleSink sink) {
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        sink.put("jvm.memory.heap.used", heap.getUsed());
        sink.put("jvm.memory.heap.committed", heap.getCommitted());
        sink.put("jvm.memory.heap.max", heap.getMax());
        sink.put("jvm.memory.heap.init", heap.getInit());
        long max = heap.getMax();
        sink.put("jvm.memory.heap.usage", max > 0 ? heap.getUsed() * 100.0 / max : 0);

        MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
        sink.put("jvm.memory.nonheap.used", nonHeap.getUsed());
        sink.put("jvm.memory.nonheap.committed", nonHeap.getCommitted());
        sink.put("jvm.memory.nonheap.max", nonHeap.getMax());

        for (MemoryPoolMXBean pool : poolMXBeans) {
            MemoryUsage usage = pool.getUsage();
            if (usage == null) {
                continue;
            }
            String prefix = "jvm.memory.pool." + sanitize(pool.getName());
            sink.put(prefix + ".used", usage.getUsed());
            sink.put(prefix + ".committed", usage.getCommitted());
        }
    }

    private void sampleGc(SampleSink sink) {
        long lastPause = -1;
        long lastEnd = -1;
        for (GarbageCollectorMXBean gc : gcMXBeans) {
            String prefix = "jvm.gc." + sanitize(gc.getName());
            sink.put(prefix + ".count", gc.getCollectionCount());
            sink.put(prefix + ".time", gc.getCollectionTime());
            if (!(gc instanceof com.sun.management.GarbageCollectorMXBean)) {
                continue;
            }
            com.sun.management.GcInfo gcInfo = null;
            if (gc instanceof com.sun.management.GarbageCollectorMXBean) {
                gcInfo = ((com.sun.management.GarbageCollectorMXBean) gc).getLastGcInfo();
            }
            if (gcInfo == null || gcInfo.getEndTime() < lastEnd) {
                continue;
            }
            lastEnd = gcInfo.getEndTime();
            lastPause = Math.max(0, gcInfo.getEndTime() - gcInfo.getStartTime());
        }
        if (lastPause >= 0) {
            sink.put("jvm.gc.pause.last", lastPause);
            if (lastPause > maxPauseObserved) {
                maxPauseObserved = lastPause;
            }
            sink.put("jvm.gc.pause.max", maxPauseObserved);
        }
    }

    private void sampleClassLoading(SampleSink sink) {
        sink.put("jvm.class.loaded", classLoadingMXBean.getLoadedClassCount());
        sink.put("jvm.class.unloaded", classLoadingMXBean.getUnloadedClassCount());
        sink.put("jvm.class.total", classLoadingMXBean.getTotalLoadedClassCount());
    }

    private void sampleThreads(SampleSink sink) {
        sink.put("jvm.thread.count", threadMXBean.getThreadCount());
        sink.put("jvm.thread.peak", threadMXBean.getPeakThreadCount());
        sink.put("jvm.thread.daemon", threadMXBean.getDaemonThreadCount());
        sink.put("jvm.thread.totalstarted", threadMXBean.getTotalStartedThreadCount());

        long[] ids = threadMXBean.getAllThreadIds();
        ThreadInfo[] infos = ids.length == 0 ? new ThreadInfo[0] : threadMXBean.getThreadInfo(ids, 0);
        int[] counts = new int[Thread.State.values().length];
        int alive = 0;
        for (ThreadInfo info : infos) {
            if (info == null) {
                continue;
            }
            alive++;
            counts[info.getThreadState().ordinal()]++;
        }
        Thread.State[] states = Thread.State.values();
        for (int i = 0; i < states.length; i++) {
            sink.put("jvm.thread.state." + states[i].name().toLowerCase(), counts[i]);
        }
        sink.put("jvm.thread.alive", alive);
    }

    private void sampleRuntime(SampleSink sink) {
        sink.put("jvm.runtime.uptime", runtimeMXBean.getUptime());
        if (compilationMXBean != null) {
            sink.put("jvm.compile.total.time", compilationMXBean.getTotalCompilationTime());
        }
        if (sunOsMXBean != null && sunOsMXBean.getProcessCpuTime() > 0) {
            sink.put("jvm.process.cpu.time", sunOsMXBean.getProcessCpuTime());
        }
        sink.attribute("jvm.name", runtimeMXBean.getVmName());
        sink.attribute("jvm.vendor", runtimeMXBean.getVmVendor());
        sink.attribute("jvm.version", runtimeMXBean.getVmVersion());
        sink.attribute("jvm.pid", String.valueOf(ProcessHandle.current().pid()));
        sink.attribute("jvm.start.time", String.valueOf(runtimeMXBean.getStartTime()));
        sink.attribute("jvm.args", String.join(" ", runtimeMXBean.getInputArguments()));
    }
}
