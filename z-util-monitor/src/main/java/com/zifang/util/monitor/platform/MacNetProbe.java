package com.zifang.util.monitor.platform;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * macOS 网络探针 —— 调 {@code netstat}。
 * <p>
 * 网卡计数读 {@code netstat -ibn} 的 Ibytes/Obytes/Ipkts/Opkts 列；每张网卡
 * 会有 Link/inet/inet6 多行，计数相同，取首行即可。TCP 状态读
 * {@code netstat -an -p tcp} 的末列文本状态，天然就是归一化状态名。
 * </p>
 * <p>
 * 注意：本探针每次调用都 fork 一个 {@code netstat} 进程，采样周期不宜低于
 * 秒级；Linux 侧读 {@code /proc} 没有这个开销。
 * </p>
 *
 * @author zifang
 */
public class MacNetProbe implements NetStatsProbe {

    @Override
    public Map<String, long[]> interfaceCounters() {
        List<String> lines = Exec.lines("netstat", "-ibn");
        return lines.isEmpty() ? new LinkedHashMap<>() : parseInterfaceCounters(lines);
    }

    @Override
    public Map<String, Integer> tcpStates() {
        List<String> lines = Exec.lines("netstat", "-an", "-p", "tcp");
        return lines.isEmpty() ? new LinkedHashMap<>() : parseTcpStates(lines);
    }

    /**
     * 解析 {@code netstat -ibn} 的输出。
     * <p>
     * 完整行是 11 列（Name Mtu Network Address Ipkts Ierrs Ibytes Opkts Oerrs
     * Obytes Coll），但 Link 行没有 Address 列、只有 10 列，因此按行长度
     * 自适应偏移，不能钉死下标。
     * </p>
     *
     * @param lines ListString类型参数
     *
     * @return MapStringLongArray类型返回值，值为 {接收字节, 发送字节, 接收包数, 发送包数}
     */
    static Map<String, long[]> parseInterfaceCounters(List<String> lines) {
        Map<String, long[]> counters = new LinkedHashMap<>();
        for (String line : lines) {
            String[] columns = line.trim().split("\\s+");
            if (columns.length < 10) {
                continue;
            }
            String name = columns[0];
            if (counters.containsKey(name)) {
                // 同一张网卡的 inet/inet6 行与 Link 行计数相同，只认首行
                continue;
            }
            // Address 列在 Link 行缺席，有它时后面各列整体右移一位
            int s = columns.length >= 11 ? 1 : 0;
            try {
                long bytesReceived = Long.parseLong(columns[5 + s]);
                long bytesSent = Long.parseLong(columns[8 + s]);
                long packetsReceived = Long.parseLong(columns[3 + s]);
                long packetsSent = Long.parseLong(columns[6 + s]);
                counters.put(name, new long[]{bytesReceived, bytesSent, packetsReceived, packetsSent});
            } catch (NumberFormatException ignored) {
                // "-" 之类的占位列直接跳过该行
            }
        }
        return counters;
    }

    /**
     * 解析 {@code netstat -an -p tcp} 的输出，取末列状态名计数。
     *
     * @param lines ListString类型参数
     *
     * @return MapStringInteger类型返回值，键为 ESTABLISHED 等状态名
     */
    static Map<String, Integer> parseTcpStates(List<String> lines) {
        Map<String, Integer> states = new LinkedHashMap<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || !trimmed.startsWith("tcp")) {
                continue;
            }
            String[] columns = trimmed.split("\\s+");
            if (columns.length < 6) {
                continue;
            }
            states.merge(columns[columns.length - 1], 1, Integer::sum);
        }
        return states;
    }
}
