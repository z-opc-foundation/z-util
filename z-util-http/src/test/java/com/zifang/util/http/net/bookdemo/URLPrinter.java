package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * URLPrinter类。
 */
public class URLPrinter {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try {
            URL u = new URL("http://www.oreilly.com/");
            URLConnection uc = u.openConnection();
            System.out.println(uc.getURL());
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}