package com.zifang.util.monitor.platform;

/**
 * 平台探针工厂。
 * <p>
 * 按 {@code os.name} 把当前操作系统映射到对应的探针实现，采集器只面向
 * {@link HostProbe} / {@link NetStatsProbe} 接口取数，不写任何平台判断：
 * </p>
 * <ul>
 *   <li>Linux → {@link LinuxHostProbe} / {@link LinuxNetProbe}（读 {@code /proc}）；</li>
 *   <li>macOS → {@link MacHostProbe} / {@link MacNetProbe}（{@code UnixOperatingSystemMXBean}
 *       + {@code sysctl} / {@code netstat}）；</li>
 *   <li>其余（含 Windows）→ 降级实现，所有读数返回 -1 / 空 Map，对应指标不产出。</li>
 * </ul>
 * <p>
 * 探针都无状态，实例按平台缓存一份。
 * </p>
 *
 * @author zifang
 */
public final class Platform {

    private static final HostProbe HOST = detectHost();
    private static final NetStatsProbe NET = detectNet();

    private Platform() {
    }

    /**
     * 宿主机探针。
     *
     * @return HostProbe类型返回值
     */
    public static HostProbe host() {
        return HOST;
    }

    /**
     * 网络探针。
     *
     * @return NetStatsProbe类型返回值
     */
    public static NetStatsProbe net() {
        return NET;
    }

    private static String osName() {
        return System.getProperty("os.name", "").toLowerCase();
    }

    private static HostProbe detectHost() {
        String os = osName();
        if (os.contains("linux")) {
            return new LinuxHostProbe();
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return new MacHostProbe();
        }
        return new NoopHostProbe();
    }

    private static NetStatsProbe detectNet() {
        String os = osName();
        if (os.contains("linux")) {
            return new LinuxNetProbe();
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return new MacNetProbe();
        }
        return new NoopNetProbe();
    }
}
