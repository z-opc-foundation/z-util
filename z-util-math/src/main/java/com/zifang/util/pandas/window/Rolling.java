package com.zifang.util.pandas.window;

import com.zifang.util.pandas.Series;

import java.util.function.Function;

/**
 * Rolling 类 - 滚动窗口计算
 * 对标 pandas.Series.rolling
 */
public class Rolling {

    private final Series series;
    private final int window;
    private final int minPeriods;
    private final boolean center;

    /**
     * Rolling方法。
     * * @param series Series类型参数
     *
     * @param window int类型参数
     */
    public Rolling(Series series, int window) {
        this(series, window, window, false);
    }

    /**
     * Rolling方法。
     * * @param series Series类型参数
     *
     * @param window     int类型参数
     * @param minPeriods int类型参数
     * @param center     boolean类型参数
     */
    public Rolling(Series series, int window, int minPeriods, boolean center) {
        this.series = series;
        this.window = window;
        this.minPeriods = minPeriods;
        this.center = center;
    }

    /**
     * 计算窗口内的均值
     */
    public Series mean() {
        return applyWindow(values -> {
            double sum = 0;
            for (double v : values) {
                sum += v;
            }
            return sum / values.length;
        });
    }

    /**
     * 计算窗口内的和
     */
    public Series sum() {
        return applyWindow(values -> {
            double sum = 0;
            for (double v : values) {
                sum += v;
            }
            return sum;
        });
    }

    /**
     * 计算窗口内的最大值
     */
    public Series max() {
        return applyWindow(values -> {
            double max = Double.NEGATIVE_INFINITY;
            for (double v : values) {
                if (v > max) max = v;
            }
            return max;
        });
    }

    /**
     * 计算窗口内的最小值
     */
    public Series min() {
        return applyWindow(values -> {
            double min = Double.POSITIVE_INFINITY;
            for (double v : values) {
                if (v < min) min = v;
            }
            return min;
        });
    }

    /**
     * 计算窗口内的标准差
     */
    public Series std() {
        return applyWindow(values -> {
            double mean = 0;
            for (double v : values) {
                mean += v;
            }
            mean /= values.length;

            double sumSquares = 0;
            for (double v : values) {
                sumSquares += Math.pow(v - mean, 2);
            }
            return Math.sqrt(sumSquares / values.length);
        });
    }

    /**
     * 计算窗口内的方差
     */
    public Series var() {
        return applyWindow(values -> {
            double mean = 0;
            for (double v : values) {
                mean += v;
            }
            mean /= values.length;

            double sumSquares = 0;
            for (double v : values) {
                sumSquares += Math.pow(v - mean, 2);
            }
            return sumSquares / values.length;
        });
    }

    /**
     * 计算窗口内非空值的数量
     */
    public Series count() {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            int start = center ? Math.max(0, i - window / 2) : Math.max(0, i - window + 1);
            int end = center ? Math.min(data.length, i + window / 2 + 1) : Math.min(data.length, i + 1);

            int count = 0;
            for (int j = start; j < end; j++) {
                if (!Double.isNaN(data[j])) {
                    count++;
                }
            }
            result[i] = count >= minPeriods ? count : Double.NaN;
        }

        return new Series(result, series.index(), series.name(), null);
    }

    /**
     * 应用自定义聚合函数
     */
    public Series apply(Function<Series, Double> func) {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            int start = center ? Math.max(0, i - window / 2) : Math.max(0, i - window + 1);
            int end = center ? Math.min(data.length, i + window / 2 + 1) : Math.min(data.length, i + 1);

            double[] windowData = new double[end - start];
            int validCount = 0;
            for (int j = start; j < end; j++) {
                if (!Double.isNaN(data[j])) {
                    windowData[validCount++] = data[j];
                }
            }

            if (validCount >= minPeriods) {
                double[] validData = new double[validCount];
                System.arraycopy(windowData, 0, validData, 0, validCount);
                result[i] = func.apply(new Series(validData));
            } else {
                result[i] = Double.NaN;
            }
        }

        return new Series(result, series.index(), series.name(), null);
    }

    // ==================== 私有方法 ====================

    private Series applyWindow(Function<double[], Double> aggregator) {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            int start = center ? Math.max(0, i - window / 2) : Math.max(0, i - window + 1);
            int end = center ? Math.min(data.length, i + window / 2 + 1) : Math.min(data.length, i + 1);

            double[] windowData = new double[end - start];
            int validCount = 0;
            for (int j = start; j < end; j++) {
                if (!Double.isNaN(data[j])) {
                    windowData[validCount++] = data[j];
                }
            }

            if (validCount >= minPeriods) {
                double[] validData = new double[validCount];
                System.arraycopy(windowData, 0, validData, 0, validCount);
                result[i] = aggregator.apply(validData);
            } else {
                result[i] = Double.NaN;
            }
        }

        return new Series(result, series.index(), series.name(), null);
    }
}
