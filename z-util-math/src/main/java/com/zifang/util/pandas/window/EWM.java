package com.zifang.util.pandas.window;

import com.zifang.util.pandas.Series;

/**
 * EWM (Exponentially Weighted Moving) 类 - 指数加权移动窗口计算
 * 对标 pandas.Series.ewm
 * 对每个值赋予指数递减的权重，越近的值权重越高
 */
public class EWM {

    private final Series series;
    private final double alpha;        // 衰减因子
    private final double com;          // 质心
    private final double span;         // 跨度
    private final double halflife;     // 半衰期
    private final boolean adjust;      // 是否调整初始值
    private final boolean ignoreNaN;   // 是否忽略 NaN
    private final int minPeriods;      // 最小周期数

    /**
     * EWM方法。
     * * @param series Series类型参数
     *
     * @param alpha double类型参数
     */
    public EWM(Series series, double alpha) {
        this(series, alpha, -1, -1, -1, true, false, 0);
    }

    /**
     * EWM方法。
     * * @param series Series类型参数
     *
     * @param alpha      double类型参数
     * @param com        double类型参数
     * @param span       double类型参数
     * @param halflife   double类型参数
     * @param adjust     boolean类型参数
     * @param ignoreNaN  boolean类型参数
     * @param minPeriods int类型参数
     */
    public EWM(Series series, double alpha, double com, double span, double halflife,
               boolean adjust, boolean ignoreNaN, int minPeriods) {
        this.series = series;
        this.alpha = alpha;
        this.com = com;
        this.span = span;
        this.halflife = halflife;
        this.adjust = adjust;
        this.ignoreNaN = ignoreNaN;
        this.minPeriods = minPeriods;
    }

    /**
     * 使用 COM (质心) 创建 EWM
     */
    public static EWM com(Series series, double com) {
        double alpha = 1 / (1 + com);
        return new EWM(series, alpha, com, -1, -1, true, false, 0);
    }

    /**
     * 使用跨度创建 EWM
     */
    public static EWM span(Series series, double span) {
        double alpha = 2 / (span + 1);
        return new EWM(series, alpha, -1, span, -1, true, false, 0);
    }

    /**
     * 使用半衰期创建 EWM
     */
    public static EWM halflife(Series series, double halflife) {
        double alpha = 1 - Math.exp(-Math.log(2) / halflife);
        return new EWM(series, alpha, -1, -1, halflife, true, false, 0);
    }

    // ==================== 核心计算方法 ====================

    /**
     * 计算指数加权移动平均 (EWMA)
     */
    public Series mean() {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        double ewma = 0;
        double weightSum = 0;
        int validCount = 0;

        for (int i = 0; i < data.length; i++) {
            if (Double.isNaN(data[i])) {
                if (ignoreNaN) {
                    result[i] = Double.NaN;
                    continue;
                } else {
                    result[i] = Double.NaN;
                    ewma = 0;
                    weightSum = 0;
                    validCount = 0;
                    continue;
                }
            }

            double weight = adjust ? Math.pow(1 - alpha, validCount) : alpha;

            if (validCount == 0) {
                ewma = data[i];
                weightSum = 1;
            } else {
                ewma = alpha * data[i] + (1 - alpha) * ewma;
            }

            if (adjust) {
                weightSum += weight;
                result[i] = ewma / weightSum;
            } else {
                result[i] = ewma;
            }

            validCount++;
        }

        return new Series(result, series.index(), series.name(), null);
    }

    /**
     * 计算指数加权移动方差
     */
    public Series var() {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        double mean = 0;
        double var = 0;
        double weightSum = 0;
        int validCount = 0;

        for (int i = 0; i < data.length; i++) {
            if (Double.isNaN(data[i])) {
                if (ignoreNaN) {
                    result[i] = Double.NaN;
                    continue;
                } else {
                    result[i] = Double.NaN;
                    mean = 0;
                    var = 0;
                    weightSum = 0;
                    validCount = 0;
                    continue;
                }
            }

            double weight = adjust ? Math.pow(1 - alpha, validCount) : 1;

            if (validCount == 0) {
                mean = data[i];
                var = 0;
                weightSum = weight;
            } else {
                double delta = data[i] - mean;
                mean += alpha * delta;
                var = (1 - alpha) * var + alpha * delta * (data[i] - mean);
                weightSum += weight;
            }

            if (adjust) {
                result[i] = var / weightSum;
            } else {
                result[i] = var;
            }

            validCount++;
        }

        return new Series(result, series.index(), series.name(), null);
    }

    /**
     * 计算指数加权移动标准差
     */
    public Series std() {
        Series variance = var();
        return variance.apply(x -> Math.sqrt(x));
    }

    /**
     * 计算指数加权移动和
     */
    public Series sum() {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        double sum = 0;
        double weightSum = 0;
        int validCount = 0;

        for (int i = 0; i < data.length; i++) {
            if (Double.isNaN(data[i])) {
                if (ignoreNaN) {
                    result[i] = Double.NaN;
                    continue;
                } else {
                    result[i] = Double.NaN;
                    sum = 0;
                    weightSum = 0;
                    validCount = 0;
                    continue;
                }
            }

            double weight = adjust ? Math.pow(1 - alpha, validCount) : 1;

            if (validCount == 0) {
                sum = data[i];
                weightSum = weight;
            } else {
                sum = sum * (1 - alpha) + data[i];
            }

            if (adjust) {
                weightSum += weight;
                result[i] = sum / weightSum;
            } else {
                result[i] = sum;
            }

            validCount++;
        }

        return new Series(result, series.index(), series.name(), null);
    }
}
