package com.zifang.util.core.lang.primitive;

/**
 * @author zifang
 */
public class Longs {

    /**
     * byte数组 转 long
     */
    public static long of(byte[] bytes) {
        long longa = 0;
        for (int i = 0; i < bytes.length; i++) {
            // 移位和清零
            longa += ((long) (bytes[i] & 0xff) << i * 8);
        }

        return longa;
    }

}
