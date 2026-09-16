package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDPEchoServer类。
 */
public class UDPEchoServer extends UDPServer {

    public final static int DEFAULT_PORT = 7;

    /**
     * UDPEchoServer方法。
     */
    public UDPEchoServer() {
        super(DEFAULT_PORT);
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        UDPServer server = new UDPEchoServer();
        Thread t = new Thread(server);
        t.start();
    }

    @Override
    /**
     * respond方法。
     *      * @param socket DatagramSocket类型参数
     * @param packet DatagramPacket类型参数
     */
    public void respond(DatagramSocket socket, DatagramPacket packet) throws IOException {
        DatagramPacket outgoing = new DatagramPacket(packet.getData(), packet.getLength(), packet.getAddress(),
                packet.getPort());
        socket.send(outgoing);
    }
}