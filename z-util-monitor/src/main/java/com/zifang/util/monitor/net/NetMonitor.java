package com.zifang.util.monitor.net;

import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.SampleSink;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 网络监控采集器。
 * <p>
 * 采集网卡维度的累计收发量并换算成速率，以及 TCP 连接的状态分布。
 * </p>
 * <p>
 * 网卡计数来自 {@code /proc/net/dev}（Linux）：标准 JDK 的 {@link NetworkInterface}
 * 只暴露网卡名称、地址、MTU 这类静态信息，拿不到累计收发字节。因此 Linux 之外
 * 的平台只产出网卡静态信息与 TCP 连接数，不产出流量指标。
 * </p>
 * <p>
 * 速率是采集器自己算的：记住上一轮的累计值与时间戳，与本轮相减除以间隔秒数。
 * 因此第一轮采样的速率恒为 0。
 * </p>
 *
 * @author zifang
 */
public class NetMonitor implements MetricsSampler {

    private static final Path PROC_NET_DEV = Paths.get("/proc/net/dev");
    private static final Path PROC_NET_TCP = Paths.get("/proc/net/tcp");
    private static final Path PROC_NET_TCP6 = Paths.get("/proc/net/tcp6");

    private final List<MetricDefinition> definitions;

    private long lastBytesReceived = -1;
    private long lastBytesSent = -1;
    private long lastTimestamp = -1;

    /**
     * NetMonitor方法。
     */
    public NetMonitor() {
        this.definitions = buildDefinitions();
    }

    private static MetricDefinition def(String name, String description, String unit, MetricDefinition.Type type) {
        return new MetricDefinition(name, MetricDefinition.Category.NET, unit, type, description);
    }

    private List<MetricDefinition> buildDefinitions() {
        List<MetricDefinition> list = new ArrayList<>();
        list.add(def("net.interface.count", "网卡数量", "count", MetricDefinition.Type.GAUGE));
        list.add(def("net.bytes.received", "网卡累计接收字节", "bytes", MetricDefinition.Type.COUNTER));
        list.add(def("net.bytes.sent", "网卡累计发送字节", "bytes", MetricDefinition.Type.COUNTER));
        list.add(def("net.packets.received", "网卡累计接收包数", "count", MetricDefinition.Type.COUNTER));
        list.add(def("net.packets.sent", "网卡累计发送包数", "count", MetricDefinition.Type.COUNTER));
        list.add(def("net.rx.rate", "网卡接收速率", "bytes/s", MetricDefinition.Type.GAUGE));
        list.add(def("net.tx.rate", "网卡发送速率", "bytes/s", MetricDefinition.Type.GAUGE));
        list.add(def("net.tcp.total", "TCP 连接总数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("net.tcp.established", "TCP 已建立连接数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("net.tcp.time_wait", "TCP TIME_WAIT 连接数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("net.tcp.close_wait", "TCP CLOSE_WAIT 连接数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("net.tcp.listen", "TCP 监听套接字数", "count", MetricDefinition.Type.GAUGE));
        list.add(def("net.tcp.syn_sent", "TCP SYN_SENT 连接数", "count", MetricDefinition.Type.GAUGE));
        return list;
    }

    @Override
    public String id() {
        return "net";
    }

    @Override
    public Collection<MetricDefinition> definitions() {
        return Collections.unmodifiableList(definitions);
    }

    @Override
    public void sample(SampleSink sink) {
        List<String> names = new ArrayList<>();
        int interfaceCount = 0;

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            interfaces = null;
        }
        if (interfaces != null) {
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                interfaceCount++;
                names.add(networkInterface.getName());
                String prefix = "net.iface." + sanitize(networkInterface.getName());
                try {
                    sink.put(prefix + ".up", networkInterface.isUp() ? 1 : 0);
                    sink.put(prefix + ".mtu", networkInterface.getMTU());
                    sink.put(prefix + ".loopback", networkInterface.isLoopback() ? 1 : 0);
                } catch (SocketException ignored) {
                    // 单张网卡读不到状态就跳过它的静态指标
                }
            }
        }
        sink.put("net.interface.count", interfaceCount);

        Map<String, long[]> counters = readInterfaceCounters();
        if (!counters.isEmpty()) {
            long bytesReceived = 0;
            long bytesSent = 0;
            long packetsReceived = 0;
            long packetsSent = 0;
            for (Map.Entry<String, long[]> entry : counters.entrySet()) {
                long[] values = entry.getValue();
                bytesReceived += values[0];
                bytesSent += values[1];
                packetsReceived += values[2];
                packetsSent += values[3];
                String prefix = "net.iface." + sanitize(entry.getKey());
                sink.put(prefix + ".bytes.received", values[0]);
                sink.put(prefix + ".bytes.sent", values[1]);
            }
            sink.put("net.bytes.received", bytesReceived);
            sink.put("net.bytes.sent", bytesSent);
            sink.put("net.packets.received", packetsReceived);
            sink.put("net.packets.sent", packetsSent);

            long now = System.currentTimeMillis();
            if (lastTimestamp > 0) {
                double elapsedSeconds = (now - lastTimestamp) / 1000.0;
                if (elapsedSeconds > 0) {
                    sink.put("net.rx.rate", Math.max(0, bytesReceived - lastBytesReceived) / elapsedSeconds);
                    sink.put("net.tx.rate", Math.max(0, bytesSent - lastBytesSent) / elapsedSeconds);
                }
            }
            lastBytesReceived = bytesReceived;
            lastBytesSent = bytesSent;
            lastTimestamp = now;
        }

        Map<String, Integer> tcpStates = readTcpStates();
        if (!tcpStates.isEmpty()) {
            int total = 0;
            for (Integer count : tcpStates.values()) {
                total += count;
            }
            sink.put("net.tcp.total", total);
            sink.put("net.tcp.established", tcpStates.getOrDefault("01", 0));
            sink.put("net.tcp.time_wait", tcpStates.getOrDefault("06", 0));
            sink.put("net.tcp.close_wait", tcpStates.getOrDefault("08", 0));
            sink.put("net.tcp.listen", tcpStates.getOrDefault("0A", 0));
            sink.put("net.tcp.syn_sent", tcpStates.getOrDefault("02", 0));
        }

        sink.attribute("net.interface.names", String.join(",", names));
    }

    static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * 读取每张网卡的累计计数。
     *
     * @return MapStringLongArray类型返回值，值为 {接收字节, 发送字节, 接收包数, 发送包数}
     */
    private static Map<String, long[]> readInterfaceCounters() {
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
     * 读取 TCP 连接状态分布。
     *
     * @return MapStringInteger类型返回值，键为十六进制状态码
     */
    private static Map<String, Integer> readTcpStates() {
        Map<String, Integer> states = new LinkedHashMap<>();
        readTcpFile(PROC_NET_TCP, states);
        readTcpFile(PROC_NET_TCP6, states);
        return states;
    }

    private static void readTcpFile(Path path, Map<String, Integer> states) {
        if (!Files.isReadable(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            states.putAll(parseTcpStates(lines));
        } catch (IOException ignored) {
            // 读不到就不产出 TCP 指标
        }
    }

    /**
     * 解析 {@code /proc/net/tcp} 的内容。
     *
     * @param lines ListString类型参数
     *
     * @return MapStringInteger类型返回值，键为十六进制状态码
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
            states.merge(columns[3], 1, Integer::sum);
        }
        return states;
    }
}
