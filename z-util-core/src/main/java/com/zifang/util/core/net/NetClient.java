package com.zifang.util.core.net;

import java.io.Closeable;
import java.io.IOException;

/**
 * 网络客户端接口。
 * <p>
 * 定义网络客户端的基本行为规范。
 *
 * @author zifang
 */
public interface NetClient extends Closeable {

    /**
     * 连接到指定主机和端口。
     *
     * @param host 主机地址
     * @param port 端口号
     * @throws IOException 连接异常
     */
    void connect(String host, int port) throws IOException;

    /**
     * 连接到指定主机和端口，指定超时时间。
     *
     * @param host      主机地址
     * @param port      端口号
     * @param timeoutMs 超时时间（毫秒）
     * @throws IOException 连接异常
     */
    void connect(String host, int port, int timeoutMs) throws IOException;

    /**
     * 判断是否已连接。
     *
     * @return 是否连接
     */
    boolean isConnected();

    /**
     * 断开连接。
     */
    void disconnect();

    /**
     * 发送数据。
     *
     * @param data 字节数据
     * @return 发送的字节数
     * @throws IOException 发送异常
     */
    int send(byte[] data) throws IOException;

    /**
     * 接收数据。
     *
     * @param buffer 接收缓冲区
     * @return 接收的字节数
     * @throws IOException 接收异常
     */
    int receive(byte[] buffer) throws IOException;

    /**
     * 接收数据，指定长度。
     *
     * @param buffer 接收缓冲区
     * @param offset 起始偏移
     * @param length 接收长度
     * @return 接收的字节数
     * @throws IOException 接收异常
     */
    int receive(byte[] buffer, int offset, int length) throws IOException;
}