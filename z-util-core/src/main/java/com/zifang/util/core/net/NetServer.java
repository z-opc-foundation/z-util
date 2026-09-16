package com.zifang.util.core.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;

/**
 * 网络服务端接口。
 * <p>
 * 定义网络服务端的基本行为规范。
 *
 * @author zifang
 */
public interface NetServer extends Closeable {

    /**
     * 绑定到指定端口。
     *
     * @param port 端口号
     * @throws IOException 绑定异常
     */
    void bind(int port) throws IOException;

    /**
     * 绑定到指定地址和端口。
     *
     * @param address socket地址
     * @throws IOException 绑定异常
     */
    void bind(SocketAddress address) throws IOException;

    /**
     * 绑定到指定端口，设置 backlog。
     *
     * @param port    端口号
     * @param backlog 连接队列长度
     * @throws IOException 绑定异常
     */
    void bind(int port, int backlog) throws IOException;

    /**
     * 判断是否已启动。
     *
     * @return 是否启动
     */
    boolean isStarted();

    /**
     * 停止服务。
     */
    void stop();

    /**
     * 获取 ServerSocket。
     *
     * @return ServerSocket
     */
    ServerSocket getServerSocket();

    /**
     * 设置客户端连接处理器。
     *
     * @param handler 处理器
     */
    void setConnectionHandler(ConnectionHandler handler);

    /**
     * 开始接受连接（同步阻塞）。
     *
     * @throws IOException 接受异常
     */
    void start() throws IOException;

    /**
     * 异步启动接受连接。
     *
     * @param handler 异步处理器
     */
    void startAsync(ConnectionHandler handler);

    /**
     * 客户端连接处理器接口。
     */
    interface ConnectionHandler {
        void handle(NetClient client) throws IOException;
    }
}