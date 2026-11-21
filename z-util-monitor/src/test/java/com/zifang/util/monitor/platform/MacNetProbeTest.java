package com.zifang.util.monitor.platform;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * macOS 网络探针的纯解析测试。
 * <p>
 * 用 {@code netstat -ibn} 与 {@code netstat -an -p tcp} 的真实输出样例
 * 验证解析逻辑，不依赖运行平台。
 * </p>
 */
class MacNetProbeTest {

    private static final List<String> NETSTAT_IBN = Arrays.asList(
            "Name  Mtu   Network       Address            Ipkts Ierrs     Ibytes    Opkts Oerrs     Obytes  Coll",
            "lo0   16384 <Link#1>                        8901     0  1234567    8902     0  2345678     0",
            "lo0   16384 fe80::%lo0/64 fe80::1%lo0           -     -        -        -     -        -     -",
            "en0   1500  <Link#4>    a1:b2:c3:d4:e5:f6  12345     0  987654321  54321     0  876543210     0",
            "en0   1500  fe80::%en0/64 fe80::a1b2:c3d4    12345     0  987654321  54321     0  876543210     0");

    private static final List<String> NETSTAT_TCP = Arrays.asList(
            "Active Internet connections (including servers)",
            "Proto Recv-Q Send-Q  Local Address          Foreign Address        (state)",
            "tcp4       0      0  127.0.0.1.6463         127.0.0.1.55712        ESTABLISHED",
            "tcp4       0      0  127.0.0.1.55712        127.0.0.1.6463         TIME_WAIT",
            "tcp46      0      0  *.8080                 *.*                    LISTEN",
            "tcp4       0      0  10.0.0.2.51000         10.0.0.1.443           CLOSE_WAIT",
            "tcp4       0      0  10.0.0.2.51001         10.0.0.1.443           SYN_SENT");

    @Test
    /**
     * testParseInterfaceCountersTakesFirstRowPerName方法。
     */
    void testParseInterfaceCountersTakesFirstRowPerName() {
        Map<String, long[]> counters = MacNetProbe.parseInterfaceCounters(NETSTAT_IBN);

        assertEquals(2, counters.size());

        long[] lo = counters.get("lo0");
        assertEquals(1234567L, lo[0]);
        assertEquals(2345678L, lo[1]);
        assertEquals(8901L, lo[2]);
        assertEquals(8902L, lo[3]);

        long[] en0 = counters.get("en0");
        assertEquals(987654321L, en0[0]);
        assertEquals(876543210L, en0[1]);
        assertEquals(12345L, en0[2]);
        assertEquals(54321L, en0[3]);
    }

    @Test
    /**
     * testParseTcpStates方法。
     */
    void testParseTcpStates() {
        Map<String, Integer> states = MacNetProbe.parseTcpStates(NETSTAT_TCP);

        assertEquals(1, states.get(NetStatsProbe.STATE_ESTABLISHED));
        assertEquals(1, states.get(NetStatsProbe.STATE_TIME_WAIT));
        assertEquals(1, states.get(NetStatsProbe.STATE_LISTEN));
        assertEquals(1, states.get(NetStatsProbe.STATE_CLOSE_WAIT));
        assertEquals(1, states.get(NetStatsProbe.STATE_SYN_SENT));
        assertEquals(5, states.size());
    }

    @Test
    /**
     * testParseTcpStatesIgnoresHeaderAndNonTcp方法。
     */
    void testParseTcpStatesIgnoresHeaderAndNonTcp() {
        Map<String, Integer> states = MacNetProbe.parseTcpStates(Arrays.asList(
                "Active Internet connections",
                "Proto Recv-Q Send-Q  Local Address          Foreign Address        (state)",
                "udp4       0      0  *.5353                 *.*",
                "tcp4       0      0  127.0.0.1.80           127.0.0.1.50000        ESTABLISHED"));

        assertEquals(1, states.size());
        assertEquals(1, states.get(NetStatsProbe.STATE_ESTABLISHED));
    }
}
