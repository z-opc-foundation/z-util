package com.zifang.util.core.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统环境相关工具类。
 */
public class SystemUtil {

    private SystemUtil() {
    }

    /**
     * 获取当前进程的 PID。
     *
     * @return 进程ID
     */
    public static long getProcessId() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        int atIndex = name.indexOf('@');
        if (atIndex > 0) {
            return Long.parseLong(name.substring(0, atIndex));
        }
        return -1;
    }

    /**
     * 获取操作系统名称。
     *
     * @return 操作系统名称
     */
    public static String getOsName() {
        return System.getProperty("os.name", "unknown");
    }

    /**
     * 获取操作系统架构。
     *
     * @return 系统架构（如 x86_64）
     */
    public static String getOsArch() {
        return System.getProperty("os.arch", "unknown");
    }

    /**
     * 获取操作系统版本。
     *
     * @return 系统版本
     */
    public static String getOsVersion() {
        return System.getProperty("os.version", "unknown");
    }

    /**
     * 获取 JVM 启动时间戳。
     *
     * @return 启动时间（毫秒）
     */
    public static long getStartTime() {
        return ManagementFactory.getRuntimeMXBean().getStartTime();
    }

    /**
     * 获取 JVM 运行时间。
     *
     * @return 运行时间（毫秒）
     */
    public static long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * 获取可用处理器数量。
     *
     * @return 处理器数量
     */
    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * 获取主机名。
     *
     * @return 主机名
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /**
     * 获取主机 IP 地址。
     *
     * @return IP 地址
     */
    public static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /**
     * 获取系统负载。
     *
     * @return 系统负载平均值
     */
    public static double getSystemLoadAverage() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        return os.getSystemLoadAverage();
    }

    /**
     * 获取 JVM 最大内存。
     *
     * @return 最大内存（字节）
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 获取 JVM 空闲内存。
     *
     * @return 空闲内存（字节）
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * 获取 JVM 总内存。
     *
     * @return 总内存（字节）
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }
}