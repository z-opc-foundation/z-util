package com.zifang.util.monitor.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Linux 网络探针 —— 读 {@code /proc/net/dev} 与 {@code /proc/net/tcp(/tcp6)}。
 * <p>
 * {@code /proc} 里的 TCP 状态是十六进制码（01/06/0A…），本探针负责翻译成
 * {@link NetStatsProbe} 约定的归一化状态名，调用方不再看到码表。
 * </p>
 *
 * @author zifang
 */
public class LinuxNetProbe implements NetStatsProbe {

    private static final Path PROC_NET_DEV = Paths.get("/proc/net/dev");
    private static final Path PROC_NET_TCP = Paths.get("/proc/net/tcp");
    private static final Path PROC_NET_TCP6 = Paths.get("/proc/net/tcp6");

    /** {@code /proc/net/tcp} 的 st 列十六进制码 → 归一化状态名。 */
    private static final Map<String, String> HEX_STATES = buildHexStates();

    @Override
    public Map<String, long[]> interfaceCounters() {
        if (!Files.isReadable(PROC_NET_DEV)) {
            return Collections.emptyMap();
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(PROC_NET_DEV, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return Collections.emptyMap();
        }
        return parseInterfaceCounters(lines);
    }

    @Override
    public Map<String, Integer> tcpStates() {
        Map<String, Integer> states = new LinkedHashMap<>();
        accumulateTcpFile(PROC_NET_TCP, states);
        accumulateTcpFile(PROC_NET_TCP6, states);
        return states;
    }

    /**
     * 解析 {@code /proc/net/dev} 的内容。
     *
     * @param lines ListString类型参数
     *
     * @return MapStringLongArray类型返回值，值为 {接收字节, 发送字节, 接收包数, 发送包数}
     */
    static Map<String, long[]> parseInterfaceCounters(List<String> lines) {
        Map<String, long[]> counters = new LinkedHashMap<>();
        for (int i = 2; i < lines.size(); i++) {
            String line = lines.get(i);
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String name = line.substring(0, colon).trim();
            String[] columns = line.substring(colon + 1).trim().split("\\s+");
            if (columns.length < 10) {
                continue;
            }
            try {
                long bytesReceived = Long.parseLong(columns[0]);
                long packetsReceived = Long.parseLong(columns[1]);
                long bytesSent = Long.parseLong(columns[8]);
                long packetsSent = Long.parseLong(columns[9]);
                counters.put(name, new long[]{bytesReceived, bytesSent, packetsReceived, packetsSent});
            } catch (NumberFormatException ignored) {
                // 解析不了的行直接跳过
            }
        }
        return counters;
    }

    /**
     * 解析 {@code /proc/net/tcp} 的内容，状态码翻译成归一化状态名。
     *
     * @param lines ListString类型参数
     *
     * @return MapStringInteger类型返回值，键为 ESTABLISHED 等归一化状态名，
     *         码表里没有的状态保留原始十六进制码
     */
    static Map<String, Integer> parseTcpStates(List<String> lines) {
        Map<String, Integer> states = new LinkedHashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] columns = line.split("\\s+");
            if (columns.length < 4) {
                continue;
            }
            String name = HEX_STATES.getOrDefault(columns[3], columns[3]);
            states.merge(name, 1, Integer::sum);
        }
        return states;
    }

    private static void accumulateTcpFile(Path path, Map<String, Integer> states) {
        if (!Files.isReadable(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (Map.Entry<String, Integer> entry : parseTcpStates(lines).entrySet()) {
                states.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        } catch (IOException ignored) {
            // 读不到就不产出该文件的连接
        }
    }

    private static Map<String, String> buildHexStates() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("01", NetStatsProbe.STATE_ESTABLISHED);
        map.put("02", NetStatsProbe.STATE_SYN_SENT);
        map.put("03", NetStatsProbe.STATE_SYN_RECV);
        map.put("04", "FIN_WAIT1");
        map.put("05", "FIN_WAIT2");
        map.put("06", NetStatsProbe.STATE_TIME_WAIT);
        map.put("07", NetStatsProbe.STATE_CLOSED);
        map.put("08", NetStatsProbe.STATE_CLOSE_WAIT);
        map.put("09", "LAST_ACK");
        map.put("0A", NetStatsProbe.STATE_LISTEN);
        map.put("0B", "CLOSING");
        return Collections.unmodifiableMap(map);
    }
}
