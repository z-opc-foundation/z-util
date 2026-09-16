package com.zifang.util.proxy.classloader;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());
        System.out.println(date);
    }

}
