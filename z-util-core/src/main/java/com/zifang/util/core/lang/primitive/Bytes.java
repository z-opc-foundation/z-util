package com.zifang.util.core.lang.primitive;

import java.util.Arrays;

/**
 * Byte 工具类。
 * <p>
 * 提供 byte 类型的常用操作方法。
 *
 * @author zifang
 */
public class Bytes {

    private Bytes() {
    }

    /**
     * 将字节数组用指定分隔符连接成字符串。
     */
    public static String join(byte[] array, String delimiter) {
        if (array == null || array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(delimiter);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * 将多个字节数组合并为一个。
     */
    public static byte[] concat(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] arr : arrays) {
            if (arr != null) {
                totalLength += arr.length;
            }
        }
        byte[] result = new byte[totalLength];
        int index = 0;
        for (byte[] arr : arrays) {
            if (arr != null) {
                System.arraycopy(arr, 0, result, index, arr.length);
                index += arr.length;
            }
        }
        return result;
    }

    /**
     * 反转字节数组。
     */
    public static byte[] reverse(byte[] array) {
        if (array == null || array.length <= 1) {
            return array != null ? array : new byte[0];
        }
        byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }

    /**
     * 将字节数组转换为十六进制字符串。
     */
    public static String toHex(byte[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组。
     */
    public static byte[] fromHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 计算字节数组的和（按无符号值计算）。
     */
    public static int sum(byte[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        int sum = 0;
        for (byte b : array) {
            sum += (b & 0xFF);
        }
        return sum;
    }

    /**
     * 计算字节数组的平均值（按无符号值计算）。
     */
    public static double average(byte[] array) {
        if (array == null || array.length == 0) {
            return 0.0;
        }
        return (double) sum(array) / array.length;
    }

    /**
     * 获取字节数组中的最小值。
     */
    public static byte min(byte[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        byte min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * 获取字节数组中的最大值。
     */
    public static byte max(byte[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        byte max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * 判断字节数组是否包含指定字节。
     */
    public static boolean contains(byte[] array, byte value) {
        if (array == null || array.length == 0) {
            return false;
        }
        for (byte b : array) {
            if (b == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找字节在数组中首次出现的索引。
     */
    public static int indexOf(byte[] array, byte value) {
        if (array == null || array.length == 0) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 按指定分隔符分割字节数组。
     */
    public static byte[][] split(byte[] array, byte delimiter) {
        if (array == null || array.length == 0) {
            return new byte[0][];
        }
        return Arrays.stream(new String(array, 0, array.length).split(String.valueOf((char) delimiter)))
                .map(s -> s.getBytes())
                .toArray(byte[][]::new);
    }
}