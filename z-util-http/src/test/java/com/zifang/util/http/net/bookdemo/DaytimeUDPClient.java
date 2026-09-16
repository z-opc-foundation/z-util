package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * DaytimeUDPClient类。
 */
public class DaytimeUDPClient {

    private final static int PORT = 13;
    private static final String HOSTNAME = "time.nist.gov";

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(0)) {
            socket.setSoTimeout(10000);
            InetAddress host = InetAddress.getByName(HOSTNAME);
            DatagramPacket request = new DatagramPacket(new byte[1], 1, host, PORT);
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
            socket.send(request);
            socket.receive(response);
            String result = new String(response.getData(), 0, response.getLength(), StandardCharsets.US_ASCII);
            System.out.println(result);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}