package com.zifang.util.zex.bust.charpter13;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author zifang
 */
public class NioChannelTest000 {

    public static String host = "127.0.0.1";
    private static int port = 50000;

    @Test
    /**
     * server方法。
     */
    public void server() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(host, port));
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        char[] charArray = new char[1024];
        int readLength = inputStreamReader.read(charArray);
        while (readLength != -1) {
            String newString = new String(charArray, 0, readLength);
            System.out.println(newString);
            readLength = inputStreamReader.read(charArray);
        }
        inputStreamReader.close();
        inputStream.close();
        socket.close();
        serverSocket.close();
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
        outputStream.write("HelloWorld".getBytes("UTF-8"));
        outputStream.write("HelloWorld".getBytes("UTF-8"));
        // 关闭输出流
        outputStream.close();
        // 关闭连接
        socket.close();
    }
}
