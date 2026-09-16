package com.zifang.util.http.net.bookdemo;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * UDPPortScanner类。
 */
public class UDPPortScanner {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        for (int port = 1024; port <= 65535; port++) {
            try {
                // the next line will fail and drop into the catch block if
                // there is already a server running on port i
                DatagramSocket server = new DatagramSocket(port);
                server.close();
            } catch (SocketException ex) {
                System.out.println("There is a server on port " + port + ".");
            }
        }
    }
}