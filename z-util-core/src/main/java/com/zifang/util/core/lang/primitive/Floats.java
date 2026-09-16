package com.zifang.util.core.lang.primitive;

/**
 * Float 工具类。
 * <p>
 * 提供 float 类型的常用操作方法。
 *
 * @author zifang
 */
public class Floats {

    private Floats() {
    }

    /**
     * 将浮点数数组用指定分隔符连接成字符串。
     */
    public static String join(float[] array, String delimiter) {
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
     * 将多个浮点数组合并为一个。
     */
    public static float[] concat(float[]... arrays) {
        int totalLength = 0;
        for (float[] arr : arrays) {
            if (arr != null) {
                totalLength += arr.length;
            }
        }
        float[] result = new float[totalLength];
        int index = 0;
        for (float[] arr : arrays) {
            if (arr != null) {
                System.arraycopy(arr, 0, result, index, arr.length);
                index += arr.length;
            }
        }
        return result;
    }

    /**
     * 反转浮点数数组。
     */
    public static float[] reverse(float[] array) {
        if (array == null || array.length <= 1) {
            return array != null ? array : new float[0];
        }
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }

    /**
     * 将浮点数数组转换为字符串。
     */
    public static String toString(float[] array) {
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
     * 计算浮点数数组的和。
     */
    public static double sum(float[] array) {
        if (array == null || array.length == 0) {
            return 0.0;
        }
        double sum = 0;
        for (float f : array) {
            sum += f;
        }
        return sum;
    }

    /**
     * 计算浮点数数组的平均值。
     */
    public static double average(float[] array) {
        if (array == null || array.length == 0) {
            return 0.0;
        }
        return sum(array) / array.length;
    }

    /**
     * 获取浮点数数组中的最小值。
     */
    public static float min(float[] array) {
        if (array == null || array.length == 0) {
            return 0f;
        }
        float min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    /**
     * 获取浮点数数组中的最大值。
     */
    public static float max(float[] array) {
        if (array == null || array.length == 0) {
            return 0f;
        }
        float max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    /**
     * 判断浮点数数组是否包含指定值。
     */
    public static boolean contains(float[] array, float value) {
        if (array == null || array.length == 0) {
            return false;
        }
        for (float f : array) {
            if (Float.compare(f, value) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找浮点数在数组中首次出现的索引。
     */
    public static int indexOf(float[] array, float value) {
        if (array == null || array.length == 0) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (Float.compare(array[i], value) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 将字符串解析为浮点数。
     */
    public static Float parseFloat(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return Float.parseFloat(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}