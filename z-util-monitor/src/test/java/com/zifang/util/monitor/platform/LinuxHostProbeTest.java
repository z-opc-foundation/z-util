package com.zifang.util.monitor.platform;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Linux 宿主机探针的纯解析测试。
 * <p>
 * 这些解析路径只在 Linux 生效，macOS / Windows 的开发机跑不到，
 * 因此用 {@code /proc} 文本样例直接验证解析逻辑。
 * </p>
 */
class LinuxHostProbeTest {

    @Test
    /**
     * testParseHostUptimeMillis方法。
     */
    void testParseHostUptimeMillis() {
        assertEquals(123456780L, LinuxHostProbe.parseHostUptimeMillis("123456.78 987654.32"));
        assertEquals(1500L, LinuxHostProbe.parseHostUptimeMillis("1.5"));
        assertEquals(-1L, LinuxHostProbe.parseHostUptimeMillis("not a number"));
        assertEquals(-1L, LinuxHostProbe.parseHostUptimeMillis(null));
    }

    @Test
    /**
     * testParseFileDescriptorLimit方法。
     */
    void testParseFileDescriptorLimit() {
        List<String> limits = Arrays.asList(
                "Limit                     Soft Limit           Hard Limit           Units",
                "Max cpu time              unlimited            unlimited            seconds",
                "Max file size             unlimited            unlimited            bytes",
                "Max open files            1048576              1048576              files",
                "Max processes             63138                63138                processes");

        assertEquals(1048576L, LinuxHostProbe.parseFileDescriptorLimit(limits));
        assertEquals(-1L, LinuxHostProbe.parseFileDescriptorLimit(Arrays.asList("Limit  Soft Limit")));
        assertEquals(-1L, LinuxHostProbe.parseFileDescriptorLimit(Arrays.asList()));
    }
}
