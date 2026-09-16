package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * LocalPortScanner类。
 */
public class LocalPortScanner {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        for (int port = 1; port <= 65535; port++) {
            try {
                // the next line will fail and drop into the catch block if
                // there is already a server running on the port
                ServerSocket server = new ServerSocket(port);
            } catch (IOException ex) {
                System.out.println("There is a server on port " + port + ".");
            }
        }
    }
}