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
import java.net.*;

/**
 * URLTest类。
 */
public class URLTest {

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

    /**
     * writeOutStream方法。
     * * @param outputStream OutputStream类型参数
     *
     * @return static void类型返回值
     */
    public static void writeOutStream(OutputStream outputStream) throws IOException {

        String content = "HTTP/1.1 200 OK\r\n" +
                "content-Type: application/json\r\n" +
                "Server: Apache\r\n" +
                "\r\n" +
                "{\"a\":\"吃饭\"}";
        int i = 0;
        outputStream.write(content.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Test
    /**
     * test0方法。
     */
    public void test0() throws URISyntaxException, IOException {
        URL url = new URL("http://aa:123@localhost:8080/search?name=doge#anchor-1");
        print(url);
    }

    private void print(URL url) throws IOException {
        System.out.println("protocol: " + url.getProtocol());
        System.out.println("userInfo: " + url.getUserInfo());
        System.out.println("host: " + url.getHost());
        System.out.println("port: " + url.getPort());
        System.out.println("path: " + url.getPath());
        System.out.println("query: " + url.getQuery());
        System.out.println("ref: " + url.getRef());
        System.out.println("file: " + url.getFile());
        System.out.println("defaultPort: " + url.getDefaultPort());
        System.out.println("authority: " + url.getAuthority());

    }

    @Test
    /**
     * test2方法。
     */
    public void test2() throws IOException {
        URL u = new URL("http://www.baidu.com");
        InputStream inputStream = u.openStream();
        byte[] b = new byte[inputStream.available()];
        inputStream.read(b);
        System.out.println(new String(b));
    }

    @Test
    /**
     * client方法。
     */
    public void client() throws IOException {
        ServerSocket server = new ServerSocket(50000);
        Socket socket = null;
        while (true) {
            socket = server.accept();
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            new Thread(new Runnable() {
                @Override
                /**
                 * run方法。
                 */
                public void run() {
                    try {
                        printMessageFromInputStream(inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                /**
                 * run方法。
                 */
                public void run() {
                    try {
                        writeOutStream(outputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Test
    /**
     * test3方法。
     */
    public void test3() {
        try {
            URL url = new URL("http://127.0.0.1:50000");
            URLConnection connection = url.openConnection();
            //connection.setDoOutput(true);

            System.out.println("content: " + connection.getContent());
            System.out.println("Content-Type: " + connection.getContentType());
            System.out.println("Content-Length: " + connection.getContentLength());
            System.out.println("Content-LengthLong: " + connection.getContentLengthLong());
            System.out.println("Content-encoding: " + connection.getContentEncoding());
            System.out.println("Date: " + connection.getDate());
            System.out.println("Expires: " + connection.getExpiration());
            System.out.println("Last-modified: " + connection.getLastModified());

            InputStream inputStream = connection.getInputStream();
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            System.out.println(new String(b));
        } catch (IOException e) {

        }
    }

    @Test
    /**
     * test4方法。
     */
    public void test4() throws IOException {
        URL url = new URL("http://www.baidu.com");
        URLConnection connection = url.openConnection();

        // 获得响应头
        System.out.println("Content-Type: " + connection.getContentType());
        System.out.println("Content-Length: " + connection.getContentLength());
        System.out.println("Content-LengthLong: " + connection.getContentLengthLong());
        System.out.println("Content-encoding: " + connection.getContentEncoding());
        System.out.println("Date: " + connection.getDate());
        System.out.println("Expires: " + connection.getExpiration());
        System.out.println("Last-modified: " + connection.getLastModified());

        InputStream inputStream = connection.getInputStream();
        byte[] b = new byte[inputStream.available()];
        inputStream.read(b);
        System.out.println(new String(b));
    }


}
