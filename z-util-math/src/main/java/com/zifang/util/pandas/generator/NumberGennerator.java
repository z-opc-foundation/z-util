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
        if (limit < 0) {
            throw new IllegalArgumentException("limit 不能为负: " + limit);
        }
        boolean[] isPrime = new boolean[limit];
        // 先把 2 及以上全标成「可能是素数」，再用筛法逐个划掉合数。
        // 0 和 1 不是素数 —— 循环从 2 开始，它们保持 false。
        for (int i = 2; i < limit; i++) {
            isPrime[i] = true;
        }
        // i 从 2 起，若是素数就把它的倍数标记为合数，
        // 从 i*i 开始可以跳过更小的倍数（它们已被更小的素数筛过）。
        for (int i = 2; (long) i * i < limit; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j < limit; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        return isPrime;
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
