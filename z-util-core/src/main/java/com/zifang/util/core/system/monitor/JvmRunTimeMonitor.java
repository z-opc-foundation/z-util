package com.zifang.util.core.system.monitor;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.Objects;

/**
 * JvmRunTimeMonitor类。
 */
public class JvmRunTimeMonitor {

    /**
     * 总的物理内存
     */
    public static long TotalMemorySize;

    private static OperatingSystemMXBean osmxb;

    private static int kb = 1024;

    static {
        try {
            osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            TotalMemorySize = osmxb.getTotalPhysicalMemorySize() / kb;
        } catch (Exception e) {
            System.out.println("获取系统信息失败");
            e.printStackTrace();
        }
    }

    /**
     * 已使用的物理内存
     */
    public final static long usedMemory() {
        if (Objects.nonNull(osmxb)) {
            return (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / kb;
        }
        return 0;
    }


    /**
     * 获取JVM内存总量
     */
    public final static long JVMtotalMem() {
        return Runtime.getRuntime().totalMemory() / kb;
    }

    /**
     * 虚拟机空闲内存量
     */
    public final static long JVMfreeMem() {
        return Runtime.getRuntime().freeMemory() / kb;
    }

    /**
     * 虚拟机使用最大内存量
     */
    public final static long JVMmaxMem() {
        return Runtime.getRuntime().maxMemory() / kb;
    }
}
