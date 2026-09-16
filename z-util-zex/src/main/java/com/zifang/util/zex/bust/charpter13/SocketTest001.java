package com.zifang.util.zex.bust.charpter13;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SocketTest001类。
 */
public class SocketTest001 {

    /**
     * printMessageFromInputStream方法。
     * * @param inputStream InputStream类型参数
     *
     * @return static void类型返回值
     */
    public static void printMessageFromInputStream(InputStream inputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int len;
        while ((len = inputStream.read(bytes)) != -1) {
            System.out.print(new String(bytes, 0, len, "UTF-8"));
        }
    }

    @Test
    /**
     * client方法。
     */
    public void client() throws IOException {
        // 服务端IP地址和端口，与服务端建立连接
        Socket socket = new Socket("127.0.0.1", 50000);
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

    @Test
    /**
     * server方法。
     */
    public void server() throws IOException {
        // 监听指定的端口
        ServerSocket server = new ServerSocket(50000);
        server.setSoTimeout(1000);
//        server.bind(new InetSocketAddress());
//        server.setReceiveBufferSize();
//        server.setReuseAddress();
//        server.isBound();
//        server.isClosed();
        // accept方法将会阻塞当前进程，知道连接真的到了
        Socket socket = server.accept();
        // 建立好连接后，从socket中获取输入流，并建立缓冲区进行读取
        InputStream inputStream = socket.getInputStream();
        // 不断打印从客户端获得到的数据
        printMessageFromInputStream(inputStream);
        // 输入流关闭
        inputStream.close();
        // 关闭socket
        socket.close();
        // 关闭监听
        server.close();
    }


}