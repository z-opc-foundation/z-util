package com.zifang.util.http.net.bookdemo;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ReverseTest类。
 */
public class ReverseTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws UnknownHostException {
        InetAddress ia = InetAddress.getByName("208.201.239.100");
        System.out.println(ia.getCanonicalHostName());
    }
}
