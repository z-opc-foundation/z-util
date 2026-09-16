package com.zifang.util.core.concurrency.packages.collection;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcurrentHashMapTest类。
 */
public class ConcurrentHashMapTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ConcurrentHashMap<String, String> stringStringConcurrentHashMap = new ConcurrentHashMap<>();
        int i = 0;
        while (true) {

            if (i == 100) {
                System.out.println();
            }
            stringStringConcurrentHashMap.put("" + i, "" + i);
            i++;


        }
    }
}
