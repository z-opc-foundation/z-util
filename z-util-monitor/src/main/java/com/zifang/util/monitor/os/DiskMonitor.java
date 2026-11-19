package com.zifang.util.monitor.os;

import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.SampleSink;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 磁盘监控采集器。
 * <p>
 * 采集每个已挂载文件系统的容量信息：总量、可用量、已用量与使用率。
 * 文件系统名会被清洗成合法的指标名片段（非字母数字一律替换成下划线）。
 * </p>
 *
 * @author zifang
 */
public class DiskMonitor implements MetricsSampler {

    private final List<FileStore> fileStores;
    private final List<MetricDefinition> definitions;

    /**
     * DiskMonitor方法。
     */
    public DiskMonitor() {
        this.fileStores = resolveFileStores();
        this.definitions = buildDefinitions();
    }

    private static List<FileStore> resolveFileStores() {
        List<FileStore> stores = new ArrayList<>();
        try {
            FileSystem fileSystem = FileSystems.getDefault();
            for (FileStore store : fileSystem.getFileStores()) {
                stores.add(store);
            }
        } catch (Throwable e) {
            // 取不到挂载点就没有磁盘指标
        }
        return stores;
    }

    static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private List<MetricDefinition> buildDefinitions() {
        List<MetricDefinition> list = new ArrayList<>();
        list.add(new MetricDefinition("disk.count", MetricDefinition.Category.DISK, "count",
                MetricDefinition.Type.GAUGE, "已挂载文件系统数"));
        for (FileStore store : fileStores) {
            String prefix = "disk." + sanitize(store.name());
            list.add(new MetricDefinition(prefix + ".total", MetricDefinition.Category.DISK, "bytes",
                    MetricDefinition.Type.GAUGE, "文件系统 " + store.name() + " 总容量"));
            list.add(new MetricDefinition(prefix + ".free", MetricDefinition.Category.DISK, "bytes",
                    MetricDefinition.Type.GAUGE, "文件系统 " + store.name() + " 可用容量"));
            list.add(new MetricDefinition(prefix + ".used", MetricDefinition.Category.DISK, "bytes",
                    MetricDefinition.Type.GAUGE, "文件系统 " + store.name() + " 已用容量"));
            list.add(new MetricDefinition(prefix + ".used.percent", MetricDefinition.Category.DISK, "percent",
                    MetricDefinition.Type.GAUGE, "文件系统 " + store.name() + " 使用率"));
        }
        return list;
    }

    @Override
    public String id() {
        return "disk";
    }

    @Override
    public Collection<MetricDefinition> definitions() {
        return Collections.unmodifiableList(definitions);
    }

    @Override
    public void sample(SampleSink sink) {
        sink.put("disk.count", fileStores.size());
        for (FileStore store : fileStores) {
            String prefix = "disk." + sanitize(store.name());
            long total;
            long usable;
            try {
                total = store.getTotalSpace();
                usable = store.getUsableSpace();
            } catch (IOException e) {
                continue;
            }
            if (total <= 0) {
                continue;
            }
            sink.put(prefix + ".total", total);
            sink.put(prefix + ".free", usable);
            sink.put(prefix + ".used", total - usable);
            sink.put(prefix + ".used.percent", (total - usable) * 100.0 / total);
        }
    }
}
