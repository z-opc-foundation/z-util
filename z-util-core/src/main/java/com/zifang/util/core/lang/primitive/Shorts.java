package com.zifang.util.core.lang.primitive;

/**
 * Short 工具类。
 * <p>
 * 提供 Short 类型的常用操作方法。
 *
 * @author zifang
 */
public class Shorts {

    private Shorts() {
    }

    /**
     * 将短整数数组用指定分隔符连接成字符串。
     */
    public static String join(short[] array, String delimiter) {
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
     * 将多个短整数数组合并为一个。
     */
    public static short[] concat(short[]... arrays) {
        int totalLength = 0;
        for (short[] arr : arrays) {
            if (arr != null) {
                totalLength += arr.length;
            }
        }
        short[] result = new short[totalLength];
        int index = 0;
        for (short[] arr : arrays) {
            if (arr != null) {
                System.arraycopy(arr, 0, result, index, arr.length);
                index += arr.length;
            }
        }
        return result;
    }

    /**
     * 反转短整数数组。
     */
    public static short[] reverse(short[] array) {
        if (array == null || array.length <= 1) {
            return array != null ? array : new short[0];
        }
        short[] result = new short[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }

    /**
     * 将短整数数组转换为字符串。
     */
    public static String toString(short[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 计算短整数数组的和。
     */
    public static int sum(short[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        int sum = 0;
        for (short s : array) {
            sum += s;
        }
        return sum;
    }

    /**
     * 获取短整数数组中的最小值。
     */
    public static short min(short[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        short min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * 获取短整数数组中的最大值。
     */
    public static short max(short[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        short max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * 判断短整数数组是否包含指定值。
     */
    public static boolean contains(short[] array, short value) {
        if (array == null || array.length == 0) {
            return false;
        }
        for (short s : array) {
            if (s == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找短整数在数组中首次出现的索引。
     */
    public static int indexOf(short[] array, short value) {
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
     * 将字符串解析为短整数。
     */
    public static Short parseShort(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return Short.parseShort(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}