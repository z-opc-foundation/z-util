package com.zifang.util.monitor.platform;

import com.sun.management.UnixOperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * macOS 宿主机探针。
 * <p>
 * 文件描述符占用与上限走 HotSpot 的 {@link UnixOperatingSystemMXBean}
 * （Linux/macOS 都有，比解析 {@code lsof} 便宜得多）；开机时长没有 proc 也没有
 * bean，只能调 {@code sysctl -n kern.boottime} 拿开机纪元秒再和当前时间相减。
 * </p>
 *
 * @author zifang
 */
public class MacHostProbe implements HostProbe {

    private static final Pattern BOOT_SEC = Pattern.compile("sec\\s*=\\s*(\\d+)");

    private final UnixOperatingSystemMXBean unixOsMXBean;

    /**
     * MacHostProbe方法。
     */
    public MacHostProbe() {
        this.unixOsMXBean = resolveUnixOsMXBean();
    }

    private static UnixOperatingSystemMXBean resolveUnixOsMXBean() {
        try {
            java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            return bean instanceof UnixOperatingSystemMXBean ? (UnixOperatingSystemMXBean) bean : null;
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public long uptimeMillis() {
        List<String> out = Exec.lines("sysctl", "-n", "kern.boottime");
        for (String line : out) {
            Matcher matcher = BOOT_SEC.matcher(line);
            if (matcher.find()) {
                try {
                    long bootEpochSec = Long.parseLong(matcher.group(1));
                    return Math.max(0, System.currentTimeMillis() - bootEpochSec * 1000);
                } catch (NumberFormatException ignored) {
                    // 解析不了就当作拿不到
                }
            }
        }
        return -1;
    }

    @Override
    public int openFileDescriptorCount() {
        return unixOsMXBean == null ? -1 : (int) unixOsMXBean.getOpenFileDescriptorCount();
    }

    @Override
    public long fileDescriptorLimit() {
        return unixOsMXBean == null ? -1 : unixOsMXBean.getMaxFileDescriptorCount();
    }
}
