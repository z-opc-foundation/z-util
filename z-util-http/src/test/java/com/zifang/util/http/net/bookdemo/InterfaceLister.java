package com.zifang.util.http.net.bookdemo;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * InterfaceLister类。
 */
public class InterfaceLister {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (ni.isUp()) {
                System.out.println(ni);
                byte[] addres = ni.getHardwareAddress();
                if (addres != null) {
                    System.out.println(addres.length);
                }
            }
        }
    }
}
