package com.zifang.util.core.lang.primitive;


import com.zifang.util.core.lang.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * 提供位运算操作的工具类
 */
public class Bits {

    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 得到int 与 long 二进制字符串
     */
    public static String binaryStr(Number number) {
        if (number == null) {
            throw new NullPointerException();
        }
        if (number instanceof Integer) {
            return StringUtil.leftPad(Integer.toBinaryString(number.intValue()), Integer.SIZE, '0');
        } else if (number instanceof Long) {
            return StringUtil.leftPad(Long.toBinaryString(number.longValue()), Long.SIZE, '0');
        } else {
            return String.valueOf(number);
        }
    }

    /**
     * 判断是奇数
     */
    public static boolean isOdd(int i) {
        return (abs(i) & 1) != 0;
    }

    /**
     * 判断是偶数
     */
    public static boolean isEven(int i) {
        return !isOdd(i);
    }

    /**
     * 获得平均值
     */
    public static int avg(int x, int y) {
        return (x & y) + ((x ^ y) >> 1);
    }

    /**
     * 是否是2的次方
     */
    public static boolean isPowFrom2(int x) {
        return ((x & (x - 1)) == 0) && (x != 0);
    }

    /**
     * 求绝对值
     */
    public static int abs(int x) {
        int y = x >> 31;
        return (x ^ y) - y;
    }

    /**
     * 快速取模
     */
    public static int mod(int x, int mod) {
        if (isPowFrom2(mod)) {
            return x & (mod - 1);
        } else {
            return x % mod;
        }
    }

    /**
     * 获得n以下，最接近cap的2的倍数
     */
    public static int multipleLess(int n) {
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        return (n + 1) >> 1;
    }

    /**
     * 获得cap以上，最接近cap的2的倍数
     */
    public static int multipleMore(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 从byte里获取特定位数上的值
     */
    public static int at(int i, int k) {
        return 1; // todo
    }


    /**
     * @author: zifang
     * @description: 将指定bit位设置为0
     * @time: 2020/9/5 17:15
     * @params: [flagSet, index] 请求参数
     * @return: long 响应参数
     */
    public static long setFalse(long flagSet, int index) {
        long mask = ~(1L << index);
        return (flagSet & (mask));
    }

    /**
     * @author: zifang
     * @description: 将指定bit位设置为1
     * @time: 2020/9/5 17:15
     * @params: [flagSet, index] 请求参数
     * @return: long 响应参数
     */
    public static long setTrue(long flagSet, int index) {
        return (flagSet | (1L << index));
    }

    /**
     * @author: zifang
     * @description: 获取指定位 1为true, 0为false
     * @time: 2020/9/5 17:17
     * @params: [flagSet, index] 请求参数
     * @return: boolean 响应参数
     */
    public static boolean getFlag(long flagSet, int index) {
        return ((flagSet & (1L << index)) != 0);
    }

    /**
     * @author: zifang
     * @description: 将指定bit位设置为0
     * @time: 2020/9/5 17:15
     * @params: [flagSet, index] 请求参数
     * @return: long 响应参数
     */
    public static long setFalse(long flagSet, byte index) {
        return setFalse(flagSet, Integer.valueOf(index));
    }

    /**
     * @author: zifang
     * @description: 将指定bit位设置为1
     * @time: 2020/9/5 17:15
     * @params: [flagSet, index] 请求参数
     * @return: long 响应参数
     */
    public static long setTrue(long flagSet, byte index) {
        return setTrue(flagSet, Integer.valueOf(index));
    }

    /**
     * @author: zifang
     * @description: 获取指定位 1为true, 0为false
     * @time: 2020/9/5 17:17
     * @params: [flagSet, index] 请求参数
     * @return: boolean 响应参数
     */
    public static boolean getFlag(long flagSet, byte index) {
        return getFlag(flagSet, Integer.valueOf(index));
    }

    /**
     * @author: zifang
     * @description: 获取所有为true的下标集合
     * @time: 2020/12/7 18:07
     * @params: [flagSet] 请求参数
     * @return: boolean 响应参数
     */
    public static Set<Byte> getAllTrueIndex(long flagSet) {
        Set<Byte> indexSet = new HashSet<>();
        for (byte index = 0; index < 64; index++) {
            if (getFlag(flagSet, index)) {
                indexSet.add(index);
            }
        }
        return indexSet;
    }


    /**
     * @author: zifang
     * @description: 获取所有为false的下标集合
     * @time: 2020/12/7 18:07
     * @params: [flagSet] 请求参数
     * @return: boolean 响应参数
     */
    public static Set<Byte> getAllFalseIndex(long flagSet) {
        Set<Byte> indexSet = new HashSet<>();
        for (byte index = 0; index < 64; index++) {
            if (!getFlag(flagSet, index)) {
                indexSet.add(index);
            }
        }
        return indexSet;
    }
}
