package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/**
 * AsynchronousChargenClient类。
 */
public class AsynchronousChargenClient {

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
            client.connect(address);

            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(74);
                CompletionHandler<Integer, ByteBuffer> handler = new LineHandler();
                try {
                    client.read(buffer, buffer, handler);
                } catch (ReadPendingException ex) {
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static class LineHandler implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        /**
         * completed方法。
         *      * @param result int类型参数
         * @param buffer ByteBuffer类型参数
         */
        public void completed(Integer result, ByteBuffer buffer) {
            buffer.flip();
            WritableByteChannel out = Channels.newChannel(System.out);
            try {
                out.write(buffer);
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }

        @Override
        /**
         * failed方法。
         *      * @param ex Throwable类型参数
         * @param attachment ByteBuffer类型参数
         */
        public void failed(Throwable ex, ByteBuffer attachment) {
            System.err.println(ex.getMessage());
        }

    }
}