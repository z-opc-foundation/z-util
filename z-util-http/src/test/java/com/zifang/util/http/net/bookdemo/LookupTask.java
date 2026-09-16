package com.zifang.util.http.net.bookdemo;

import java.net.InetAddress;
import java.util.concurrent.Callable;

/**
 * LookupTask类。
 */
public class LookupTask implements Callable<String> {

    private String line;

    /**
     * LookupTask方法。
     * * @param line String类型参数
     */
    public LookupTask(String line) {
        this.line = line;
    }

    @Override
    /**
     * call方法。
     * @return String类型返回值
     */
    public String call() {
        try {
            // separate out the IP address
            int index = line.indexOf(' ');
            String address = line.substring(0, index);
            String theRest = line.substring(index);
            String hostname = InetAddress.getByName(address).getHostName();
            return hostname + " " + theRest;
        } catch (Exception ex) {
            return line;
        }
    }
}