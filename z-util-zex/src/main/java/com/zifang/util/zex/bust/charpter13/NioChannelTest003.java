package com.zifang.util.zex.bust.charpter13;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author zifang
 */
public class NioChannelTest003 {

    public static String host = "127.0.0.1";
    private static int port = 50000;

    @Test
    /**
     * server方法。
     */
    public void server() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(host, port));
        //serverSocketChannel.configureBlocking(false);
        SocketChannel socketChannel = serverSocketChannel.accept();
        ByteBuffer buteBuffer = ByteBuffer.allocate(10);
        int readLength = socketChannel.read(buteBuffer);
        while (readLength != -1) {
            String newString = new String(buteBuffer.array());
            System.out.println(newString);
            buteBuffer.flip();
            readLength = socketChannel.read(buteBuffer);
        }
        socketChannel.close();
        serverSocketChannel.close();
    }

    @Test
    /**
     * client方法。
     */
    public void client() throws IOException {
        // 服务端IP地址和端口，与服务端建立连接
        Socket socket = new Socket(host, port);
        // 建立连接后获得输出流
        OutputStream outputStream = socket.getOutputStream();
        // 往输出流内写入数据
        outputStream.write("HelloWorld\n".getBytes("UTF-8"));
        outputStream.write("HelloWorld\n".getBytes("UTF-8"));
        // 关闭输出流
        outputStream.close();
        // 关闭连接
        socket.close();
    }


}
