package com.zifang.util.http.net.bookdemo;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Weblog类。
 */
public class Weblog {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try (FileInputStream fin = new FileInputStream(args[0]);
             Reader in = new InputStreamReader(fin);
             BufferedReader bin = new BufferedReader(in)) {

            for (String entry = bin.readLine(); entry != null; entry = bin.readLine()) {
                // separate out the IP address
                int index = entry.indexOf(' ');
                String ip = entry.substring(0, index);
                String theRest = entry.substring(index);

                // Ask DNS for the hostname and print it out
                try {
                    InetAddress address = InetAddress.getByName(ip);
                    System.out.println(address.getHostName() + theRest);
                } catch (UnknownHostException ex) {
                    System.err.println(entry);
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception: " + ex);
        }
    }
}