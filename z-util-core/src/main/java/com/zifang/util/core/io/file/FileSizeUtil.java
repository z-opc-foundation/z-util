package com.zifang.util.core.io.file;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 文件大小格式化工具类
 * <p>
 * 将字节数转换为带单位的可读字符串，单位依次为B、KB、MB、GB、TB，
 * 换算基数1024，数值部分四舍五入保留两位小数。
 *
 * @author zifang
 */
public class FileSizeUtil {

    /**
     * 单位表，下标即单位级别（1024进制）
     */
    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};

    /**
     * 格式化字节数为可读字符串，保留两位小数
     * <p>
     * 例如：512 → "512B"，1536 → "1.50KB"，10L * 1024 * 1024 → "10.00MB"。
     *
     * @param size 字节数，负数按0处理
     * @return 带单位的文件大小字符串
     */
    public static String formatSize(long size) {
        return formatSize(size, 2);
    }

    /**
     * 格式化字节数为可读字符串，自定义小数位数
     * <p>
     * 小于1KB时直接以字节整数表示，不做小数格式化。
     *
     * @param size  字节数，负数按0处理
     * @param scale 数值部分保留的小数位数（0~6），小于0时按0处理
     * @return 带单位的文件大小字符串
     */
    public static String formatSize(long size, int scale) {
        if (size < 0) {
            size = 0;
        }
        if (scale < 0) {
            scale = 0;
        }
        if (size < 1024) {
            return size + UNITS[0];
        }
        BigDecimal value = new BigDecimal(size);
        BigDecimal base = new BigDecimal(1024);
        int unitIndex = 0;
        // 逐级换算，找到数值不足1024的单位（超出最大单位时停留在最大单位）
        while (value.compareTo(base) >= 0 && unitIndex < UNITS.length - 1) {
            value = value.divide(base, 16, RoundingMode.HALF_UP);
            unitIndex++;
        }
        value = value.setScale(scale, RoundingMode.HALF_UP);
        return value.toPlainString() + UNITS[unitIndex];
    }
}
