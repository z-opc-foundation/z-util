package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Daytime类。
 */
public class Daytime {

    /**
     * getDateFromNetwork方法。
     *
     * @return static StringBuilder类型返回值
     */
    public static StringBuilder getDateFromNetwork() throws IOException, ParseException {
        try (Socket socket = new Socket("time.nist.gov", 13)) {
            socket.setSoTimeout(15000);
            InputStream in = socket.getInputStream();
            StringBuilder time = new StringBuilder();
            InputStreamReader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);
            for (int c = reader.read(); c != -1; c = reader.read()) {
                time.append((char) c);
            }
            return time;
        }
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        System.out.println(getDateFromNetwork().toString());
    }
}