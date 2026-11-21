package com.zifang.util.monitor.os;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DiskMonitor 包内静态方法的测试。
 * <p>
 * {@code sanitize} 是包私有，测试只能放在同包里。
 * </p>
 */
class DiskSanitizeTest {

    @Test
    /**
     * testSanitizeMethod方法。
     */
    void testSanitizeMethod() {
        assertEquals("_dev_disk1s1", DiskMonitor.sanitize("/dev/disk1s1"));
        assertEquals("devfs", DiskMonitor.sanitize("devfs"));
    }
}
