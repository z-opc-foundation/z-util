package com.zifang.util.monitor.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 平台探针工厂测试。
 * <p>
 * 只验证「选型正确 + 降级契约成立」，具体取数逻辑归各自的解析测试。
 * </p>
 */
class PlatformTest {

    @Test
    /**
     * testProbesAreNonNull方法。
     */
    void testProbesAreNonNull() {
        assertTrue(Platform.host() != null);
        assertTrue(Platform.net() != null);
    }

    @Test
    /**
     * testProbeTypeMatchesCurrentOs方法。
     */
    void testProbeTypeMatchesCurrentOs() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux")) {
            assertTrue(Platform.host() instanceof LinuxHostProbe);
            assertTrue(Platform.net() instanceof LinuxNetProbe);
        } else if (os.contains("mac") || os.contains("darwin")) {
            assertTrue(Platform.host() instanceof MacHostProbe);
            assertTrue(Platform.net() instanceof MacNetProbe);
        } else {
            assertTrue(Platform.host() instanceof NoopHostProbe);
            assertTrue(Platform.net() instanceof NoopNetProbe);
        }
    }

    @Test
    /**
     * testNoopProbeContract方法。
     */
    void testNoopProbeContract() {
        NoopHostProbe host = new NoopHostProbe();
        assertTrue(host.uptimeMillis() == -1);
        assertTrue(host.openFileDescriptorCount() == -1);
        assertTrue(host.fileDescriptorLimit() == -1);

        NoopNetProbe net = new NoopNetProbe();
        assertFalse(!net.interfaceCounters().isEmpty());
        assertFalse(!net.tcpStates().isEmpty());
    }

    @Test
    /**
     * testLiveProbesRespectContract方法。
     * <p>
     * 当前平台的真实探针跑一遍：要么给出有效读数，要么按契约返回 -1 / 空，
     * 不允许抛异常或返回负的累计字节。
     * </p>
     */
    void testLiveProbesRespectContract() {
        long uptime = Platform.host().uptimeMillis();
        assertTrue(uptime == -1 || uptime > 0);

        int fds = Platform.host().openFileDescriptorCount();
        assertTrue(fds == -1 || fds > 0);

        Platform.net().interfaceCounters().forEach((name, counters) -> {
            assertFalse(name.isEmpty());
            assertTrue(counters.length == 4);
            assertTrue(counters[0] >= 0);
        });
        Platform.net().tcpStates().forEach((state, count) -> {
            assertFalse(state.isEmpty());
            assertTrue(count > 0);
        });
    }
}
