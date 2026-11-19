package com.zifang.util.monitor.os;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Linux {@code /proc} 解析测试。
 * <p>
 * 这些解析路径只在 Linux 上生效，macOS / Windows 的开发机跑不到，
 * 因此用真实的 {@code /proc} 文本样例直接验证解析逻辑。
 * </p>
 */
class ProcParsingTest {

    @Test
    /**
     * testParseHostUptimeMillis方法。
     */
    void testParseHostUptimeMillis() {
        assertEquals(123456780L, OsMonitor.parseHostUptimeMillis("123456.78 987654.32"));
        assertEquals(1500L, OsMonitor.parseHostUptimeMillis("1.5"));
        assertEquals(-1L, OsMonitor.parseHostUptimeMillis("not a number"));
        assertEquals(-1L, OsMonitor.parseHostUptimeMillis(null));
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

        assertEquals(1048576L, OsMonitor.parseFileDescriptorLimit(limits));
        assertEquals(-1L, OsMonitor.parseFileDescriptorLimit(Arrays.asList("Limit  Soft Limit")));
        assertEquals(-1L, OsMonitor.parseFileDescriptorLimit(Arrays.asList()));
    }

    @Test
    /**
     * testSanitizeMethod方法。
     */
    void testSanitizeMethod() {
        assertEquals("_dev_disk1s1", DiskMonitor.sanitize("/dev/disk1s1"));
        assertEquals("devfs", DiskMonitor.sanitize("devfs"));
    }
}
