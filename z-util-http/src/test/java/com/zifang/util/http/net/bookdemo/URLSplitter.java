package com.zifang.util.http.net.bookdemo;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URLSplitter类。
 */
public class URLSplitter {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        try {
            URL u = new URL("http://a@www.baidu.com:8000/aa/aa?c=s");
            System.out.println("The URL is " + u);
            System.out.println("The scheme is " + u.getProtocol());
            System.out.println("The user info is " + u.getUserInfo());

            String host = u.getHost();
            if (host != null) {
                int atSign = host.indexOf('@');
                if (atSign != -1)
                    host = host.substring(atSign + 1);
                System.out.println("The host is " + host);
            } else {
                System.out.println("The host is null.");
            }

            System.out.println("The port is " + u.getPort());
            System.out.println("The path is " + u.getPath());
            System.out.println("The ref is " + u.getRef());
            System.out.println("The query string is " + u.getQuery());
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            //System.err.println(args[i] + " is not a URL I understand.");
        }
        System.out.println();
    }
}
