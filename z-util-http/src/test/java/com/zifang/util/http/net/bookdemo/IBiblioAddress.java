package com.zifang.util.http.net.bookdemo;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IBiblioAddress类。
 */
public class IBiblioAddress {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try {
            InetAddress ibiblio = InetAddress.getByName("www.sina.com");
            InetAddress helios = InetAddress.getByName("www.sina.com.cn");
            if (ibiblio.equals(helios)) {
                System.out.println("www.ibiblio.org is the same as helios.ibiblio.org");
            } else {
                System.out.println("www.ibiblio.org is not the same as helios.ibiblio.org");
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            System.out.println("Host lookup failed.");
        }
    }
}
