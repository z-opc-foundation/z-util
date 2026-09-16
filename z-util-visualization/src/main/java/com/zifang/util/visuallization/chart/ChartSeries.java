package com.zifang.util.visuallization.chart;

import java.util.ArrayList;
import java.util.List;

/**
 * 图表数据系列
 * 用于存储和管理一个系列的数据点和标签
 */
public class ChartSeries {

    private final String name;
    private final List<Double> data;
    private final List<String> labels;

    /**
     * 创建数据系列
     *
     * @param name 系列名称
     */
    public ChartSeries(String name) {
        this.name = name;
        this.data = new ArrayList<>();
        this.labels = new ArrayList<>();
    }

    /**
     * 创建数据系列
     *
     * @param name 系列名称
     * @param data 初始数据列表
     */
    public ChartSeries(String name, List<Double> data) {
        this.name = name;
        this.data = new ArrayList<>(data);
        this.labels = new ArrayList<>();
    }

    /**
     * 获取系列名称
     *
     * @return 系列名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取数据副本
     *
     * @return 数据列表副本
     */
    public List<Double> getData() {
        return new ArrayList<>(data);
    }

    /**
     * 获取标签副本
     *
     * @return 标签列表副本
     */
    public List<String> getLabels() {
        return new ArrayList<>(labels);
    }

    /**
     * 获取数据点数量
     *
     * @return 数据点数量
     */
    public int size() {
        return data.size();
    }

    /**
     * 获取指定索引的数据值
     *
     * @param index 数据索引
     * @return 数据值
     * @throws IndexOutOfBoundsException 如果索引超出范围
     */
    public double getData(int index) {
        return data.get(index);
    }

    /**
     * 获取指定索引的标签
     *
     * @param index 标签索引
     * @return 标签字符串（如果不存在则返回索引字符串）
     */
    public String getLabel(int index) {
        if (index < labels.size()) {
            return labels.get(index);
        }
        return String.valueOf(index);
    }

    /**
     * 添加数据点
     *
     * @param value 数据值
     */
    public void addData(double value) {
        data.add(value);
    }

    /**
     * 添加带标签的数据点
     *
     * @param value 数据值
     * @param label 数据标签
     */
    public void addData(double value, String label) {
        data.add(value);
        labels.add(label);
    }

    /**
     * 清空所有数据
     */
    public void clear() {
        data.clear();
        labels.clear();
    }

    /**
     * 获取最大值
     *
     * @return 最大值（如果数据为空返回0）
     */
    public double getMax() {
        if (data.isEmpty()) return 0;
        double max = data.get(0);
        for (double v : data) {
            if (v > max) max = v;
        }
        return max;
    }

    /**
     * 获取最小值
     *
     * @return 最小值（如果数据为空返回0）
     */
    public double getMin() {
        if (data.isEmpty()) return 0;
        double min = data.get(0);
        for (double v : data) {
            if (v < min) min = v;
        }
        return min;
    }
}