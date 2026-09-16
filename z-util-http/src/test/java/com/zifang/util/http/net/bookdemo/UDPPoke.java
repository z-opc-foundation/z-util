package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * UDPPoke类。
 */
public class UDPPoke {

    private int bufferSize; // in bytes
    private int timeout; // in milliseconds
    private InetAddress host;
    private int port;

    /**
     * UDPPoke方法。
     * * @param host InetAddress类型参数
     *
     * @param port       int类型参数
     * @param bufferSize int类型参数
     * @param timeout    int类型参数
     */
    public UDPPoke(InetAddress host, int port, int bufferSize, int timeout) {
        this.bufferSize = bufferSize;
        this.host = host;
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port out of range");
        }

        this.port = port;
        this.timeout = timeout;
    }

    /**
     * UDPPoke方法。
     * * @param host InetAddress类型参数
     *
     * @param port       int类型参数
     * @param bufferSize int类型参数
     */
    public UDPPoke(InetAddress host, int port, int bufferSize) {
        this(host, port, bufferSize, 30000);
    }

    /**
     * UDPPoke方法。
     * * @param host InetAddress类型参数
     *
     * @param port int类型参数
     */
    public UDPPoke(InetAddress host, int port) {
        this(host, port, 8192, 30000);
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        InetAddress host;
        int port = 0;
        try {
            host = InetAddress.getByName(args[0]);
            port = Integer.parseInt(args[1]);
        } catch (RuntimeException | UnknownHostException ex) {
            System.out.println("Usage: java UDPPoke host port");
            return;
        }

        UDPPoke poker = new UDPPoke(host, port);
        byte[] response = poker.poke();
        if (response == null) {
            System.out.println("No response within allotted time");
            return;
        }
        String result = new String(response, StandardCharsets.US_ASCII);
        System.out.println(result);
    }

    /**
     * poke方法。
     *
     * @return byte[]类型返回值
     */
    public byte[] poke() {
        try (DatagramSocket socket = new DatagramSocket(0)) {
            DatagramPacket outgoing = new DatagramPacket(new byte[1], 1, host, port);
            socket.connect(host, port);
            socket.setSoTimeout(timeout);

            socket.send(outgoing);
            DatagramPacket incoming = new DatagramPacket(new byte[bufferSize], bufferSize);
            // next line blocks until the response is received
            socket.receive(incoming);
            int numBytes = incoming.getLength();
            byte[] response = new byte[numBytes];
            System.arraycopy(incoming.getData(), 0, response, 0, numBytes);
            return response;
        } catch (IOException ex) {
            return null;
        }
    }
}