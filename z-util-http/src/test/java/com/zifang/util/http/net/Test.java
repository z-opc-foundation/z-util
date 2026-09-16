package com.zifang.util.http.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Test类。
 */
public class Test {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws UnknownHostException {
        showIntAddress(InetAddress.getByName("www.oreilly.com"));
        showIntAddress(InetAddress.getByName("208.201.239.100"));
        showIntAddress(InetAddress.getLocalHost());
        showIntAddress(InetAddress.getByName("www.taobao.com"));
        showIntAddress(InetAddress.getByName("183.136.135.225"));
        showIntAddress(InetAddress.getByAddress(new byte[]{(byte) 180, 101, 49, 12}));
        showIntAddress(InetAddress.getByAddress("www.baidu.com", new byte[]{(byte) 180, 101, 49, 12}));
    }

    /**
     * showIntAddress方法。
     * * @param inetAddress InetAddress类型参数
     *
     * @return static void类型返回值
     */
    public static void showIntAddress(InetAddress inetAddress) {
        System.out.println("inetAddress.getAddress()               " + Arrays.toString(inetAddress.getAddress()));
        System.out.println("inetAddress.getHostName()              " + inetAddress.getHostName());
        System.out.println("inetAddress.getHostAddress()           " + inetAddress.getHostAddress());
        System.out.println("inetAddress.getCanonicalHostName()     " + inetAddress.getCanonicalHostName());
        System.out.println("----");
        System.out.println("inetAddress.isAnyLocalAddress()    " + inetAddress.isAnyLocalAddress());//通配地址
        System.out.println("inetAddress.isLoopbackAddress()    " + inetAddress.isLoopbackAddress());//回送地址
        System.out.println("inetAddress.isLinkLocalAddress()   " + inetAddress.isLinkLocalAddress());//ip6的本地连接地址
        System.out.println("inetAddress.isSiteLocalAddress()   " + inetAddress.isSiteLocalAddress());//ip6的本地网站地址
        System.out.println("inetAddress.isMulticastAddress()   " + inetAddress.isMulticastAddress());//组播地址
        System.out.println("inetAddress.isMCGlobal()           " + inetAddress.isMCGlobal());//全球组播网址
        System.out.println("inetAddress.isMCOrgLocal()         " + inetAddress.isMCOrgLocal());//组织范围组播网址
        System.out.println("inetAddress.isMCSiteLocal()        " + inetAddress.isMCSiteLocal());//网站范围组播网址
        System.out.println("inetAddress.isMCLinkLocal()        " + inetAddress.isMCLinkLocal());//子网范围内组播网址
        System.out.println("inetAddress.isMCNodeLocal()        " + inetAddress.isMCNodeLocal());//本地接口组播地址
        System.out.println("----------------------------");

    }
}
