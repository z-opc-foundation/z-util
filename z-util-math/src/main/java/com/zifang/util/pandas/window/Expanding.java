package com.zifang.util.pandas.window;

import com.zifang.util.pandas.Series;

import java.util.function.Function;

/**
 * Expanding 类 - 扩展窗口计算
 * 对标 pandas.Series.expanding
 * 从序列开头开始，窗口大小逐渐增大
 */
public class Expanding {

    private final Series series;
    private final int minPeriods;

    /**
     * Expanding方法。
     * * @param series Series类型参数
     */
    public Expanding(Series series) {
        this(series, 1);
    }

    /**
     * Expanding方法。
     * * @param series Series类型参数
     *
     * @param minPeriods int类型参数
     */
    public Expanding(Series series, int minPeriods) {
        this.series = series;
        this.minPeriods = minPeriods;
    }

    /**
     * 计算扩展窗口的均值
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
     * 计算扩展窗口的和
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
     * 计算扩展窗口的最大值
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
     * 计算扩展窗口的最小值
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
     * 计算扩展窗口的标准差
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
     * 计算扩展窗口的方差
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
     * 计算扩展窗口的计数
     */
    public Series count() {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            int count = 0;
            for (int j = 0; j <= i; j++) {
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
        return applyWindow(values -> {
            Series windowSeries = new Series(values);
            return func.apply(windowSeries);
        });
    }

    /**
     * 应用聚合函数的核心方法
     */
    private Series applyWindow(Function<double[], Double> aggregator) {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            // 扩展窗口包含从开始到当前位置的所有元素
            double[] windowData = new double[i + 1];
            int validCount = 0;

            for (int j = 0; j <= i; j++) {
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
