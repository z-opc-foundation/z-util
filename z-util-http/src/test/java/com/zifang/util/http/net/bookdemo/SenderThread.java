package com.zifang.util.http.net.bookdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

class SenderThread extends Thread {

    private InetAddress server;
    private DatagramSocket socket;
    private int port;
    private volatile boolean stopped = false;

    SenderThread(DatagramSocket socket, InetAddress address, int port) {
        this.server = address;
        this.port = port;
        this.socket = socket;
        this.socket.connect(server, port);
    }

    /**
     * halt方法。
     */
    public void halt() {
        this.stopped = true;
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                if (stopped)
                    return;
                String theLine = userInput.readLine();
                if (theLine.equals("."))
                    break;
                byte[] data = theLine.getBytes(StandardCharsets.UTF_8);
                DatagramPacket output = new DatagramPacket(data, data.length, server, port);
                socket.send(output);
                Thread.yield();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}