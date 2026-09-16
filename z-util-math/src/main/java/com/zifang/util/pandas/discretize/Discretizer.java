package com.zifang.util.pandas.discretize;

import com.zifang.util.pandas.Series;

import java.util.Arrays;

/**
 * Discretizer 类 - 数据离散化和分箱
 * 对标 pandas.cut 和 pandas.qcut
 * 提供等宽分箱和等频分箱功能
 */
public class Discretizer {

    /**
     * 等宽分箱 (cut)
     * 将数据分成等宽的区间
     *
     * @param series 输入数据
     * @param bins   分箱数量
     * @return 分箱后的标签 Series
     */
    public static Series cut(Series series, int bins) {
        double[] data = series.toArray();

        // 找出最小值和最大值
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (double v : data) {
            if (!Double.isNaN(v)) {
                if (v < min) min = v;
                if (v > max) max = v;
            }
        }

        if (min == Double.POSITIVE_INFINITY) {
            // 所有值都是 NaN
            double[] result = new double[data.length];
            Arrays.fill(result, Double.NaN);
            return new Series(result, series.index(), "cut", null);
        }

        // 计算区间宽度
        double binWidth = (max - min) / bins;

        // 分配每个数据点到对应的箱子
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            if (Double.isNaN(data[i])) {
                result[i] = Double.NaN;
            } else {
                int bin = (int) ((data[i] - min) / binWidth);
                // 确保最大值落在最后一个箱子
                if (bin >= bins) bin = bins - 1;
                result[i] = bin;
            }
        }

        return new Series(result, series.index(), "cut", null);
    }

    /**
     * 等频分箱 (qcut)
     * 将数据分成每个箱子中样本数量相等的区间
     *
     * @param series 输入数据
     * @param q      分位数数量
     * @return 分箱后的标签 Series
     */
    public static Series qcut(Series series, int q) {
        double[] data = series.toArray();

        // 过滤掉 NaN 值并排序
        double[] sorted = Arrays.stream(data)
                .filter(v -> !Double.isNaN(v))
                .sorted()
                .toArray();

        if (sorted.length == 0) {
            double[] result = new double[data.length];
            Arrays.fill(result, Double.NaN);
            return new Series(result, series.index(), "qcut", null);
        }

        // 计算分位数边界
        double[] quantiles = new double[q + 1];
        for (int i = 0; i <= q; i++) {
            double pos = (sorted.length - 1) * i / (double) q;
            int lower = (int) Math.floor(pos);
            int upper = (int) Math.ceil(pos);
            double weight = pos - lower;

            if (lower == upper) {
                quantiles[i] = sorted[lower];
            } else {
                quantiles[i] = sorted[lower] * (1 - weight) + sorted[upper] * weight;
            }
        }

        // 分配每个数据点到对应的分位数箱子
        double[] result = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            if (Double.isNaN(data[i])) {
                result[i] = Double.NaN;
            } else {
                int bin = 0;
                for (int j = 1; j < quantiles.length; j++) {
                    if (data[i] >= quantiles[j - 1] && data[i] <= quantiles[j]) {
                        bin = j - 1;
                        break;
                    }
                }
                // 确保最大值落在最后一个箱子
                if (data[i] >= quantiles[quantiles.length - 1]) {
                    bin = q - 1;
                }
                result[i] = bin;
            }
        }

        return new Series(result, series.index(), "qcut", null);
    }
}
