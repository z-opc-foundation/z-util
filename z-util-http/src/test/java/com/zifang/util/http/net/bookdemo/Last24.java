package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * Last24类。
 */
public class Last24 {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {

        // Initialize a Date object with the current date and time
        Date today = new Date();
        long millisecondsPerDay = 24 * 60 * 60 * 1000;

        for (int i = 0; i < args.length; i++) {
            try {
                URL u = new URL(args[i]);
                URLConnection uc = u.openConnection();
                System.out.println("Original if modified since: " + new Date(uc.getIfModifiedSince()));
                uc.setIfModifiedSince((new Date(today.getTime() - millisecondsPerDay)).getTime());
                System.out.println("Will retrieve file if it's modified since " + new Date(uc.getIfModifiedSince()));
                try (InputStream in = new BufferedInputStream(uc.getInputStream())) {
                    Reader r = new InputStreamReader(in);
                    int c;
                    while ((c = r.read()) != -1) {
                        System.out.print((char) c);
                    }
                    System.out.println();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }
}