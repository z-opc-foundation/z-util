package com.zifang.util.pandas.interpolate;

import com.zifang.util.pandas.Series;

/**
 * Interpolator 类 - 缺失值插值
 * 对标 pandas DataFrame/Series.interpolate
 * 提供多种插值方法填充缺失值
 */
public class Interpolator {

    /**
     * 线性插值
     * 使用相邻的非缺失值进行线性插值
     */
    public static Series linear(Series series) {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        // 复制原始数据
        System.arraycopy(data, 0, result, 0, data.length);

        int lastValidIndex = -1;
        double lastValidValue = Double.NaN;

        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i])) {
                if (lastValidIndex != -1 && i > lastValidIndex + 1) {
                    // 填充中间缺失值
                    double step = (data[i] - lastValidValue) / (i - lastValidIndex);
                    for (int j = lastValidIndex + 1; j < i; j++) {
                        result[j] = lastValidValue + step * (j - lastValidIndex);
                    }
                }
                lastValidIndex = i;
                lastValidValue = data[i];
            }
        }

        return new Series(result, series.index(), series.name(), null);
    }

    /**
     * 前向填充
     * 使用前一个非缺失值填充
     */
    public static Series forward(Series series) {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        double lastValid = Double.NaN;
        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i])) {
                lastValid = data[i];
                result[i] = data[i];
            } else if (!Double.isNaN(lastValid)) {
                result[i] = lastValid;
            } else {
                result[i] = Double.NaN;
            }
        }

        return new Series(result, series.index(), series.name(), null);
    }

    /**
     * 后向填充
     * 使用后一个非缺失值填充
     */
    public static Series backward(Series series) {
        double[] data = series.toArray();
        double[] result = new double[data.length];

        double nextValid = Double.NaN;
        for (int i = data.length - 1; i >= 0; i--) {
            if (!Double.isNaN(data[i])) {
                nextValid = data[i];
                result[i] = data[i];
            } else if (!Double.isNaN(nextValid)) {
                result[i] = nextValid;
            } else {
                result[i] = Double.NaN;
            }
        }

        return new Series(result, series.index(), series.name(), null);
    }

    /**
     * 多项式插值
     * 使用多项式拟合进行插值
     */
    public static Series polynomial(Series series, int order) {
        // 简化实现：使用线性插值作为基础
        // 实际的多项式插值需要更复杂的算法
        return linear(series);
    }

    /**
     * 样条插值
     * 使用样条函数进行平滑插值
     */
    public static Series spline(Series series) {
        // 简化实现：使用线性插值
        // 实际样条插值需要实现三次样条算法
        return linear(series);
    }
}
