package com.zifang.util.monitor.platform;

import java.util.Map;

/**
 * 网络平台探针。
 * <p>
 * 抽走网卡累计收发计数与 TCP 连接状态分布这两块「标准 JDK 拿不到」的读数 ——
 * {@link java.net.NetworkInterface} 只暴露网卡名、地址、MTU 这类静态信息。
 * Linux 实现读 {@code /proc/net/dev} 与 {@code /proc/net/tcp}；macOS 实现
 * 调 {@code netstat}；其余平台降级为不产出。
 * </p>
 * <p>
 * 两个方法的返回都按「状态归一化」约定：TCP 状态键统一用
 * {@link #STATE_ESTABLISHED} 等英文状态名，调用方不用关心底层是十六进制
 * 状态码还是 {@code netstat} 的文本。速率不在探针里算 —— 探针只出累计值，
 * 差分换算留给有状态的使用方。
 * </p>
 *
 * @author zifang
 */
public interface NetStatsProbe {

    /** TCP 已建立连接。 */
    String STATE_ESTABLISHED = "ESTABLISHED";
    /** TCP 主动打开中。 */
    String STATE_SYN_SENT = "SYN_SENT";
    /** TCP 被动打开中。 */
    String STATE_SYN_RECV = "SYN_RECV";
    /** TCP 等待关闭（主动方）。 */
    String STATE_TIME_WAIT = "TIME_WAIT";
    /** TCP 等待本地应用关闭。 */
    String STATE_CLOSE_WAIT = "CLOSE_WAIT";
    /** TCP 监听。 */
    String STATE_LISTEN = "LISTEN";
    /** TCP 全关闭。 */
    String STATE_CLOSED = "CLOSED";

    /**
     * 每张网卡的累计计数。
     *
     * @return MapStringLongArray类型返回值，键为网卡名，值为
     *         {接收字节, 发送字节, 接收包数, 发送包数}；平台不支持时返回空 Map
     */
    Map<String, long[]> interfaceCounters();

    /**
     * TCP 连接状态分布。
     *
     * @return MapStringInteger类型返回值，键为 {@link #STATE_ESTABLISHED} 等
     *         归一化状态名；平台不支持时返回空 Map
     */
    Map<String, Integer> tcpStates();
}
