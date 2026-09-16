package com.zifang.util.http.net.bookdemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * FastUDPDiscardServer类。
 */
public class FastUDPDiscardServer extends UDPServer {

    public final static int DEFAULT_PORT = 9;

    /**
     * FastUDPDiscardServer方法。
     */
    public FastUDPDiscardServer() {
        super(DEFAULT_PORT);
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        UDPServer server = new FastUDPDiscardServer();
        Thread t = new Thread(server);
        t.start();
    }

    @Override
    /**
     * respond方法。
     *      * @param socket DatagramSocket类型参数
     * @param request DatagramPacket类型参数
     */
    public void respond(DatagramSocket socket, DatagramPacket request) {
    }
}