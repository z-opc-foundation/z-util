package com.zifang.util.http.net.bookdemo;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MyAddress类。
 */
public class MyAddress {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            System.out.println(address);
        } catch (UnknownHostException ex) {
            System.out.println("Could not find this computer's address.");
        }
    }
}
