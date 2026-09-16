package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * AsynchronousFutureClient类。
 */
public class AsynchronousFutureClient {

    public static int DEFAULT_PORT = 19;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: java ChargenClient host [port]");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (RuntimeException ex) {
            port = DEFAULT_PORT;
        }

        SocketAddress address = new InetSocketAddress(args[0], port);
        try {
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            Future<Void> connected = client.connect(address);

            ByteBuffer buffer = ByteBuffer.allocate(74);
            System.out.println(buffer.hasArray());

            connected.get();

            Future<Integer> future = client.read(buffer);

            future.get();

            byte[] result = buffer.array();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}