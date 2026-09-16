package com.zifang.util.pandas.stats;

import com.zifang.util.pandas.DataFrame;
import com.zifang.util.pandas.Series;

/**
 * Correlation 类 - 相关性分析工具
 * <p>
 * 提供相关系数计算和协方差矩阵功能，对标 pandas.corr() 和 pandas.cov()。
 * 用于分析数据集中各变量之间的线性相关性强度和方向。
 *
 * <p>主要功能：
 * <ul>
 *   <li>计算 Pearson 相关系数矩阵</li>
 *   <li>计算协方差矩阵</li>
 *   <li>支持 DataFrame 整体的相关性分析</li>
 *   <li>支持两个 Series 之间的相关性分析</li>
 * </ul>
 *
 * <p>Pearson 相关系数：
 * <ul>
 *   <li>取值范围：[-1, 1]</li>
 *   <li>接近 1：强正相关</li>
 *   <li>接近 -1：强负相关</li>
 *   <li>接近 0：无明显线性相关</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * DataFrame df = Pandas.read_csv("data.csv");
 * DataFrame corrMatrix = Correlation.corr(df);
 * </pre>
 *
 * @author zifang
 * @see DataFrame
 * @see Series
 * @see Pandas
 */
public class Correlation {

    /**
     * 计算 Pearson 相关系数矩阵
     *
     * @param df 输入 DataFrame
     * @return 相关系数矩阵 DataFrame
     */
    public static DataFrame corr(DataFrame df) {
        return pearson(df);
    }

    /**
     * 计算 Pearson 相关系数矩阵
     *
     * @param df 输入 DataFrame
     * @return 相关系数矩阵 DataFrame
     */
    public static DataFrame pearson(DataFrame df) {
        java.util.List<String> columns = df.columns();
        int n = columns.size();

        // 创建相关系数矩阵
        java.util.Map<String, double[]> corrData = new java.util.LinkedHashMap<>();

        for (int i = 0; i < n; i++) {
            double[] row = new double[n];
            for (int j = 0; j < n; j++) {
                row[j] = calculatePearson(df.get(columns.get(i)), df.get(columns.get(j)));
            }
            corrData.put(columns.get(i), row);
        }

        return new DataFrame(corrData, columns.toArray(new String[0]));
    }

    /**
     * 计算两个 Series 的 Pearson 相关系数
     *
     * @param x 第一个 Series
     * @param y 第二个 Series
     * @return Pearson 相关系数，范围 [-1, 1]，如果有效数据点少于 2 个则返回 NaN
     */
    private static double calculatePearson(Series x, Series y) {
        double[] xData = x.toArray();
        double[] yData = y.toArray();

        int n = Math.min(xData.length, yData.length);

        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;
        int validCount = 0;

        for (int i = 0; i < n; i++) {
            if (!Double.isNaN(xData[i]) && !Double.isNaN(yData[i])) {
                sumX += xData[i];
                sumY += yData[i];
                sumXY += xData[i] * yData[i];
                sumX2 += xData[i] * xData[i];
                sumY2 += yData[i] * yData[i];
                validCount++;
            }
        }

        if (validCount < 2) {
            return Double.NaN;
        }

        double numerator = validCount * sumXY - sumX * sumY;
        double denominator = Math.sqrt((validCount * sumX2 - sumX * sumX) * (validCount * sumY2 - sumY * sumY));

        if (denominator == 0) {
            return Double.NaN;
        }

        return numerator / denominator;
    }

    /**
     * 计算协方差矩阵
     *
     * @param df 输入 DataFrame
     * @return 协方差矩阵 DataFrame
     */
    public static DataFrame cov(DataFrame df) {
        java.util.List<String> columns = df.columns();
        int n = columns.size();

        java.util.Map<String, double[]> covData = new java.util.LinkedHashMap<>();

        for (int i = 0; i < n; i++) {
            double[] row = new double[n];
            for (int j = 0; j < n; j++) {
                row[j] = calculateCovariance(df.get(columns.get(i)), df.get(columns.get(j)));
            }
            covData.put(columns.get(i), row);
        }

        return new DataFrame(covData, columns.toArray(new String[0]));
    }

    /**
     * 计算两个 Series 的协方差
     *
     * @param x 第一个 Series
     * @param y 第二个 Series
     * @return 协方差，如果有效数据点少于 2 个则返回 NaN
     */
    private static double calculateCovariance(Series x, Series y) {
        double[] xData = x.toArray();
        double[] yData = y.toArray();

        int n = Math.min(xData.length, yData.length);

        double sumX = 0, sumY = 0;
        int validCount = 0;

        for (int i = 0; i < n; i++) {
            if (!Double.isNaN(xData[i]) && !Double.isNaN(yData[i])) {
                sumX += xData[i];
                sumY += yData[i];
                validCount++;
            }
        }

        if (validCount < 2) {
            return Double.NaN;
        }

        double meanX = sumX / validCount;
        double meanY = sumY / validCount;

        double sumProduct = 0;
        for (int i = 0; i < n; i++) {
            if (!Double.isNaN(xData[i]) && !Double.isNaN(yData[i])) {
                sumProduct += (xData[i] - meanX) * (yData[i] - meanY);
            }
        }

        return sumProduct / (validCount - 1);
    }
}