package com.zifang.util.monitor.net;

import com.zifang.util.monitor.platform.NetStatsProbe;
import com.zifang.util.monitor.platform.Platform;
import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.MetricsSampler;
import com.zifang.util.monitor.sampling.SampleSink;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * 网络监控采集器。
 * <p>
 * 采集网卡维度的累计收发量并换算成速率，以及 TCP 连接的状态分布。
 * </p>
 * <p>
 * 网卡计数与 TCP 状态委托 {@link Platform#net()} 平台探针 —— Linux 读
 * {@code /proc/net/dev} 与 {@code /proc/net/tcp}，macOS 调 {@code netstat}，
 * 其余平台返回空 Map、流量指标自动不产出；标准 JDK 的 {@link NetworkInterface}
 * 只暴露静态信息，网卡 up/MTU 这类指标仍走 JDK。速率是采集器自己算的：
 * 记住上一轮的累计值与时间戳，与本轮相减除以间隔秒数，因此第一轮采样的速率恒缺。
 * </p>
 *
 * @author zifang
 */
public class NetMonitor implements MetricsSampler {

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

        Map<String, long[]> counters = Platform.net().interfaceCounters();
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

        Map<String, Integer> tcpStates = Platform.net().tcpStates();
        if (!tcpStates.isEmpty()) {
            int total = 0;
            for (Integer count : tcpStates.values()) {
                total += count;
            }
            sink.put("net.tcp.total", total);
            sink.put("net.tcp.established", tcpStates.getOrDefault(NetStatsProbe.STATE_ESTABLISHED, 0));
            sink.put("net.tcp.time_wait", tcpStates.getOrDefault(NetStatsProbe.STATE_TIME_WAIT, 0));
            sink.put("net.tcp.close_wait", tcpStates.getOrDefault(NetStatsProbe.STATE_CLOSE_WAIT, 0));
            sink.put("net.tcp.listen", tcpStates.getOrDefault(NetStatsProbe.STATE_LISTEN, 0));
            sink.put("net.tcp.syn_sent", tcpStates.getOrDefault(NetStatsProbe.STATE_SYN_SENT, 0));
        }

        sink.attribute("net.interface.names", String.join(",", names));
    }

    static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }
}
