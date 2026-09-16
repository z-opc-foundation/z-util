package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * DictClient类。
 */
public class DictClient {

    public static final String SERVER = "dict.org";
    public static final int PORT = 2628;
    public static final int TIMEOUT = 15000;

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        Socket socket = null;
        try {
            socket = new Socket(SERVER, PORT);
            socket.setSoTimeout(TIMEOUT);
            OutputStream out = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer = new BufferedWriter(writer);
            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            for (String word : args) {
                define(word, writer, reader);
            }

            writer.write("quit\r\n");
            writer.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        } finally { // dispose
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    static void define(String word, Writer writer, BufferedReader reader)
            throws IOException {
        writer.write("DEFINE eng-lat " + word + "\r\n");
        writer.flush();

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.startsWith("250 ")) { // OK
                return;
            } else if (line.startsWith("552 ")) { // no match
                System.out.println("No definition found for " + word);
                return;
            } else if (line.matches("\\d\\d\\d .*"))
                continue;
            else if (line.trim().equals("."))
                continue;
            else
                System.out.println(line);
        }
    }
}