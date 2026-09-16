package com.zifang.util.http.net.bookdemo;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * UTF8Test类。
 */
public class UTF8Test {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws UnsupportedEncodingException {
        String pi = "\u03C0";
        byte[] data = pi.getBytes(StandardCharsets.UTF_8);
        for (byte x : data) {
            System.out.println(Integer.toHexString(x));
        }
    }

}
