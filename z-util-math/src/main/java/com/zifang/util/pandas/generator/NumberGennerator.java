package com.zifang.util.pandas.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 数据生成器
 *
 * @author zifang
 */
public class NumberGennerator {

    /**
     * 生成质数
     *
     * @param limit 搜索的最大值
     */
    public static boolean[] primeNumber(int limit) {
        boolean[] b = new boolean[limit];

        return null;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {

        // 生成素数列表
        BufferedWriter b = new BufferedWriter(new FileWriter(new File("a.csv")));
        byte[] a = new byte[100000000];

        for (int i = 2; i < a.length; i++) {
            if (a[i] == 1) {
                continue;
            }
            for (int j = 2; j < 100000; j++) {
                if (i * j <= a.length - 1) {
                    a[i * j] = 1;
                } else {
                    break;
                }
            }
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 0) {
                b.write(i + "\n");
                b.flush();
                //System.out.println(i);
            }
        }
    }
}
