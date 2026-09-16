package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UDPServer implements Runnable {

    private final int bufferSize; // in bytes
    private final int port;
    private final Logger log = Logger.getLogger(UDPServer.class.getCanonicalName());
    private volatile boolean isShutDown = false;

    /**
     * UDPServer方法。
     * * @param port int类型参数
     *
     * @param bufferSize int类型参数
     */
    public UDPServer(int port, int bufferSize) {
        this.bufferSize = bufferSize;
        this.port = port;
    }

    /**
     * UDPServer方法。
     * * @param port int类型参数
     */
    public UDPServer(int port) {
        this(port, 8192);
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        byte[] buffer = new byte[bufferSize];
        try (DatagramSocket socket = new DatagramSocket(port)) {
            socket.setSoTimeout(10000); // check every 10 seconds for shutdown
            while (true) {
                if (isShutDown)
                    return;
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(incoming);
                    this.respond(socket, incoming);
                } catch (SocketTimeoutException ex) {
                    if (isShutDown)
                        return;
                } catch (IOException ex) {
                    log.log(Level.WARNING, ex.getMessage(), ex);
                }
            } // end while
        } catch (SocketException ex) {
            log.log(Level.SEVERE, "Could not bind to port: " + port, ex);
        }
    }

    /**
     * respond方法。
     * * @param socket DatagramSocket类型参数
     *
     * @param request DatagramPacket类型参数
     * @return abstract void类型返回值
     */
    public abstract void respond(DatagramSocket socket, DatagramPacket request) throws IOException;

    /**
     * shutDown方法。
     */
    public void shutDown() {
        this.isShutDown = true;
    }

}