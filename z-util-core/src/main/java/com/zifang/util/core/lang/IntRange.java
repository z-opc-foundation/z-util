package com.zifang.util.core.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * IntRange类。
 * 不可变整数区间，闭区间语义（含边界值）；下限或上限为 null 表示该侧边界开放。
 * 典型用途：长度/数值等多段范围校验（任一段命中即通过）与人读描述生成。
 */
public final class IntRange {

    private static final String SEGMENT_SEPARATOR = " / ";

    private final Integer min;
    private final Integer max;

    private IntRange(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    /**
     * of方法。
     *
     * @param min 下限，null 表示不限制下界
     * @param max 上限，null 表示不限制上界
     * @return IntRange类型返回值
     */
    public static IntRange of(Integer min, Integer max) {
        return new IntRange(min, max);
    }

    /**
     * contains方法。
     * 判断值是否落在区间内（闭区间）；边界为 null 表示该侧不限制；
     * 下限大于上限的区间对任何值都不命中。
     *
     * @param value 待判断值
     * @return boolean类型返回值
     */
    public boolean contains(int value) {
        if (min != null && value < min) {
            return false;
        }
        if (max != null && value > max) {
            return false;
        }
        return true;
    }

    /**
     * describe方法。
     * 生成单段人读描述：下限等于上限为固定值（如"20"）；仅有下限为"45-"；
     * 仅有上限为"-55"；两侧均有为"45-55"；两侧均为 null 返回空串。
     *
     * @return String类型返回值
     */
    public String describe() {
        if (min == null && max == null) {
            return "";
        }
        if (min != null && max != null) {
            if (min.equals(max)) {
                return String.valueOf(min);
            }
            return min + "-" + max;
        }
        if (min != null) {
            return min + "-";
        }
        return "-" + max;
    }

    /**
     * matchesAny方法。
     * 多段区间任一命中即通过：区间列表为 null 或空视为未配置，返回 true；
     * 值为 null 返回 false；列表中的 null 元素被跳过。
     *
     * @param ranges 区间列表
     * @param value  待判断值
     * @return boolean类型返回值
     */
    public static boolean matchesAny(List<IntRange> ranges, Integer value) {
        if (ranges == null || ranges.isEmpty()) {
            return true;
        }
        if (value == null) {
            return false;
        }
        for (IntRange range : ranges) {
            if (range != null && range.contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * describeAll方法。
     * 生成多段人读描述，以 " / " 拼接，跳过空描述段；列表为 null 或空返回空串。
     *
     * @param ranges 区间列表
     * @return String类型返回值
     */
    public static String describeAll(List<IntRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return "";
        }
        List<String> segments = new ArrayList<>();
        for (IntRange range : ranges) {
            if (range == null) {
                continue;
            }
            String segment = range.describe();
            if (!segment.isEmpty()) {
                segments.add(segment);
            }
        }
        return String.join(SEGMENT_SEPARATOR, segments);
    }

    /**
     * getMin方法。
     *
     * @return Integer类型返回值
     */
    public Integer getMin() {
        return min;
    }

    /**
     * getMax方法。
     *
     * @return Integer类型返回值
     */
    public Integer getMax() {
        return max;
    }

    @Override
    public String toString() {
        return describe();
    }
}
