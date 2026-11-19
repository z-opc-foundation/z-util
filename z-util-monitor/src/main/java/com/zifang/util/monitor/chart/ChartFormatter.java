package com.zifang.util.monitor.chart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 图表数值格式化。
 * <p>
 * 把带单位的原始数值转成人眼友好的短文本，用于坐标轴刻度与提示行。
 * </p>
 *
 * @author zifang
 */
public final class ChartFormatter {

    private static final String[] BYTE_UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};
    private static final String[] TIME_UNITS = {"ms", "s", "min", "h", "d"};

    private ChartFormatter() {
    }

    /**
     * 按单位格式化数值。
     *
     * @param value double类型参数
     * @param unit  String类型参数
     *
     * @return String类型返回值
     */
    public static String format(double value, String unit) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "-";
        }
        if (unit == null) {
            return plain(value);
        }
        switch (unit) {
            case "bytes":
                return bytes(value);
            case "ms":
                return duration(value);
            case "percent":
                return String.format(Locale.ROOT, "%.1f%%", value);
            case "count":
                return String.format(Locale.ROOT, "%,d", (long) value);
            default:
                return plain(value);
        }
    }

    /**
     * 字节格式化。
     *
     * @param bytes double类型参数
     *
     * @return String类型返回值
     */
    public static String bytes(double bytes) {
        if (bytes < 0) {
            return "-";
        }
        double value = bytes;
        int index = 0;
        while (value >= 1024 && index < BYTE_UNITS.length - 1) {
            value /= 1024;
            index++;
        }
        if (index == 0) {
            return (long) value + " B";
        }
        return String.format(Locale.ROOT, "%.2f %s", value, BYTE_UNITS[index]);
    }

    /**
     * 时长格式化。
     *
     * @param millis double类型参数
     *
     * @return String类型返回值
     */
    public static String duration(double millis) {
        if (millis < 0) {
            return "-";
        }
        double value = millis;
        int index = 0;
        while (value >= 1000 && index < TIME_UNITS.length - 1) {
            value /= 1000;
            index++;
        }
        if (index == 0) {
            return (long) value + " ms";
        }
        return String.format(Locale.ROOT, "%.2f %s", value, TIME_UNITS[index]);
    }

    /**
     * 时间戳格式化为 时:分:秒。
     *
     * @param timestamp long类型参数
     *
     * @return String类型返回值
     */
    public static String time(long timestamp) {
        return new SimpleDateFormat("HH:mm:ss").format(new Date(timestamp));
    }

    /**
     * 时间戳格式化为 月-日 时:分:秒。
     *
     * @param timestamp long类型参数
     *
     * @return String类型返回值
     */
    public static String dateTime(long timestamp) {
        return new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    /**
     * 坐标轴刻度用的紧凑数字。
     *
     * @param value double类型参数
     *
     * @return String类型返回值
     */
    public static String plain(double value) {
        double abs = Math.abs(value);
        if (abs >= 1_000_000_000) {
            return String.format(Locale.ROOT, "%.2fG", value / 1_000_000_000);
        }
        if (abs >= 1_000_000) {
            return String.format(Locale.ROOT, "%.2fM", value / 1_000_000);
        }
        if (abs >= 1_000) {
            return String.format(Locale.ROOT, "%.1fK", value / 1_000);
        }
        if (abs == Math.rint(abs) && abs < Long.MAX_VALUE) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
