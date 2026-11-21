package com.zifang.util.monitor.platform;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Linux 网络探针的纯解析测试。
 * <p>
 * 用 {@code /proc} 文本样例直接验证解析逻辑，重点校验十六进制状态码
 * 被翻译成归一化状态名。
 * </p>
 */
class LinuxNetProbeTest {

    private static final List<String> NET_DEV = Arrays.asList(
            "Inter-|   Receive                                                |  Transmit",
            " face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed",
            "    lo: 1234567    8901    0    0    0     0          0         0  2345678    8902    0    0    0     0       0          0",
            "  eth0: 987654321  12345    1    2    0     0          0         3  876543210  54321    4    5    0     0       0          0");

    private static final List<String> NET_TCP = Arrays.asList(
            "  sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode",
            "   0: 00000000:1F90 00000000:0000 0A 00000000:00000000 00:00000000 00000000     0        0 12345 1 0000 100 0 0 10 0",
            "   1: 0100007F:1F91 0100007F:C350 01 00000000:00000000 00:00000000 00000000  1000        0 12346 1 0000 100 0 0 10 0",
            "   2: 0100007F:1F92 0100007F:C351 06 00000000:00000000 00:00000000 00000000  1000        0 12347 1 0000 100 0 0 10 0",
            "   3: 0100007F:1F93 0100007F:C352 08 00000000:00000000 00:00000000 00000000  1000        0 12348 1 0000 100 0 0 10 0");

    @Test
    /**
     * testParseInterfaceCounters方法。
     */
    void testParseInterfaceCounters() {
        Map<String, long[]> counters = LinuxNetProbe.parseInterfaceCounters(NET_DEV);

        assertEquals(2, counters.size());
        long[] lo = counters.get("lo");
        assertEquals(1234567L, lo[0]);
        assertEquals(2345678L, lo[1]);
        assertEquals(8901L, lo[2]);
        assertEquals(8902L, lo[3]);

        long[] eth0 = counters.get("eth0");
        assertEquals(987654321L, eth0[0]);
        assertEquals(876543210L, eth0[1]);
        assertEquals(12345L, eth0[2]);
        assertEquals(54321L, eth0[3]);
    }

    @Test
    /**
     * testParseInterfaceCountersSkipsMalformedLines方法。
     */
    void testParseInterfaceCountersSkipsMalformedLines() {
        Map<String, long[]> counters = LinuxNetProbe.parseInterfaceCounters(Arrays.asList(
                "Inter-|   Receive |  Transmit",
                " face |bytes packets",
                "  bad: not a number",
                "  eth0: 100 1 0 0 0 0 0 0 200 2 0 0 0 0 0 0"));

        assertEquals(1, counters.size());
        assertEquals(100L, counters.get("eth0")[0]);
        assertEquals(200L, counters.get("eth0")[1]);
    }

    @Test
    /**
     * testParseTcpStatesMapsHexToNames方法。
     */
    void testParseTcpStatesMapsHexToNames() {
        Map<String, Integer> states = LinuxNetProbe.parseTcpStates(NET_TCP);

        assertEquals(1, states.get(NetStatsProbe.STATE_LISTEN));
        assertEquals(1, states.get(NetStatsProbe.STATE_ESTABLISHED));
        assertEquals(1, states.get(NetStatsProbe.STATE_TIME_WAIT));
        assertEquals(1, states.get(NetStatsProbe.STATE_CLOSE_WAIT));
        assertEquals(4, states.size());
    }
}
