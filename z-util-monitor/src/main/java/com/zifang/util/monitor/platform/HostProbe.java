package com.zifang.util.monitor.platform;

/**
 * 宿主机平台探针。
 * <p>
 * 抽走宿主机层面「JDK 拿不到、必须问操作系统」的那部分读数：开机时长、
 * 文件描述符占用与上限。JVM 能自己拿到的（CPU 核数、物理内存、进程 CPU 时间）
 * 不在这里，仍走 {@code com.sun.management} 扩展 bean —— 那本来就是跨平台的。
 * </p>
 * <p>
 * 每个读数都可能拿不到：不认识的平台、权限不足、命令缺失。约定是返回
 * {@code -1}（或 {@code null}）表示「本平台不产出该指标」，而不是返回 0 ——
 * 0 是一个真实的读数。
 * </p>
 *
 * @author zifang
 */
public interface HostProbe {

    /**
     * 宿主机开机时长（毫秒）。
     *
     * @return long类型返回值，拿不到时返回 -1
     */
    long uptimeMillis();

    /**
     * 当前进程已打开的文件描述符数。
     *
     * @return int类型返回值，拿不到时返回 -1
     */
    int openFileDescriptorCount();

    /**
     * 当前进程文件描述符软上限。
     *
     * @return long类型返回值，拿不到时返回 -1
     */
    long fileDescriptorLimit();
}
