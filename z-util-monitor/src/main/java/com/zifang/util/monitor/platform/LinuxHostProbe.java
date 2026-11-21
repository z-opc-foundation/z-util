package com.zifang.util.monitor.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * Linux 宿主机探针 —— 全部读 {@code /proc}。
 * <p>
 * {@code /proc/uptime} 出开机时长；{@code /proc/self/fd} 出描述符占用；
 * {@code /proc/self/limits} 出描述符软上限。任何一处读不到就按契约返回 -1。
 * </p>
 *
 * @author zifang
 */
public class LinuxHostProbe implements HostProbe {

    private static final Path PROC_UPTIME = Paths.get("/proc/uptime");
    private static final Path PROC_SELF_FD = Paths.get("/proc/self/fd");
    private static final Path PROC_SELF_LIMITS = Paths.get("/proc/self/limits");

    @Override
    public long uptimeMillis() {
        List<String> lines = readLines(PROC_UPTIME);
        return lines.isEmpty() ? -1 : parseHostUptimeMillis(lines.get(0));
    }

    @Override
    public int openFileDescriptorCount() {
        if (!Files.isDirectory(PROC_SELF_FD)) {
            return -1;
        }
        try (java.util.stream.Stream<Path> stream = Files.list(PROC_SELF_FD)) {
            return (int) stream.count();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public long fileDescriptorLimit() {
        return parseFileDescriptorLimit(readLines(PROC_SELF_LIMITS));
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
