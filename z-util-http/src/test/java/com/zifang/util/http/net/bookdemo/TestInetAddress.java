package com.zifang.util.http.net.bookdemo;

import java.net.InetAddress;

/**
 * TestInetAddress类。
 */
public class TestInetAddress {

    /**
     * InetAddress:位于java.net包下
     * 1.InetAddress用来代表IP地址，一个InetAddress的对象就代表着一个IP地址
     * 2.如何创建InetAddress的对象，getByName(String host)
     * 3.getHostName():获取IP地址对于的域名
     * getHostAddress():获取IP地址
     *
     * @param args
     * @throws Exception
     */
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        //创建一个InetAddress对象：getByName()
        InetAddress inet = InetAddress.getByName("www.baidu.com");
        //inet=InetAddress.getByName("ip");
        System.out.println(inet.getAddress().length);//address.length
        System.out.println(inet);

        System.out.println(inet.getHostName());
        System.out.println(inet.getHostAddress());
        System.out.println(inet.getAddress());

        InetAddress inet1 = InetAddress.getLocalHost();
        System.out.println(inet1);
        System.out.println(inet1.getHostName());
        System.out.println(inet1.getHostAddress());
    }
}
