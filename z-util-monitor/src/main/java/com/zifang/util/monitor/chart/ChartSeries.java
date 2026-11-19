package com.zifang.util.monitor.chart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 图表数据序列。
 * <p>
 * 一条序列 = 一个指标名 + 一串带时间戳的点。绘图时多条序列叠加在同一个坐标系里。
 * </p>
 *
 * @author zifang
 */
public final class ChartSeries {

    private final String name;
    private final String unit;
    private final List<Point> points;

    /**
     * ChartSeries方法。
     *
     * @param name   String类型参数
     * @param unit   String类型参数
     * @param points ListPoint类型参数
     */
    public ChartSeries(String name, String unit, List<Point> points) {
        this.name = name;
        this.unit = unit;
        this.points = Collections.unmodifiableList(new ArrayList<>(points));
    }

    /**
     * 创建构建器。
     *
     * @param name String类型参数
     *
     * @return Builder类型返回值
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getUnit方法。
     *
     * @return String类型返回值
     */
    public String getUnit() {
        return unit;
    }

    /**
     * getPoints方法。
     *
     * @return ListPoint类型返回值
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * isEmpty方法。
     *
     * @return boolean类型返回值
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }

    /**
     * 序列上的一个数据点。
     */
    public static final class Point {

        private final long timestamp;
        private final double value;

        /**
         * Point方法。
         *
         * @param timestamp long类型参数
         * @param value     double类型参数
         */
        public Point(long timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        /**
         * getTimestamp方法。
         *
         * @return long类型返回值
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * getValue方法。
         *
         * @return double类型返回值
         */
        public double getValue() {
            return value;
        }
    }

    /**
     * 序列构建器。
     */
    public static final class Builder {

        private final String name;
        private String unit;
        private final List<Point> points = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        /**
         * unit方法。
         *
         * @param unit String类型参数
         *
         * @return Builder类型返回值
         */
        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        /**
         * point方法。
         *
         * @param timestamp long类型参数
         * @param value     double类型参数
         *
         * @return Builder类型返回值
         */
        public Builder point(long timestamp, double value) {
            points.add(new Point(timestamp, value));
            return this;
        }

        /**
         * build方法。
         *
         * @return ChartSeries类型返回值
         */
        public ChartSeries build() {
            return new ChartSeries(name, unit, points);
        }
    }
}
