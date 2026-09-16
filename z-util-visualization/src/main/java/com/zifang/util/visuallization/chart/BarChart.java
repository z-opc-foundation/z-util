package com.zifang.util.visuallization.chart;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 条形图组件
 * 用于可视化分类数据的分布，支持多系列数据展示、坐标轴标签、图例和网格线
 */
public class BarChart extends ChartFrame {

    private static final int PADDING = 60;
    private static final int RIGHT_PADDING = 20;
    private static final int TOP_PADDING = 40;
    private static final int BOTTOM_PADDING = 60;
    private final List<ChartSeries> seriesList;
    private String xAxisLabel = "";
    private String yAxisLabel = "Value";
    private double yMax = 1;

    /**
     * 创建条形图
     *
     * @param title  图表标题
     * @param width  图表宽度
     * @param height 图表高度
     */
    public BarChart(String title, int width, int height) {
        super(title, width, height);
        this.seriesList = new ArrayList<>();
    }

    /**
     * 创建条形图（使用默认尺寸800x600）
     *
     * @param title 图表标题
     */
    public BarChart(String title) {
        this(title, 800, 600);
    }

    /**
     * 添加数据系列
     *
     * @param series 数据系列对象
     */
    public void addSeries(ChartSeries series) {
        seriesList.add(series);
        updateYRange();
    }

    /**
     * 添加数据系列
     *
     * @param name 系列名称
     * @param data 数据列表
     */
    public void addSeries(String name, List<Double> data) {
        addSeries(new ChartSeries(name, data));
    }

    /**
     * 向指定系列添加单个数据点
     *
     * @param seriesName 系列名称（不存在则创建新系列）
     * @param value      数据值
     */
    public void addData(String seriesName, double value) {
        ChartSeries series = findSeries(seriesName);
        if (series == null) {
            series = new ChartSeries(seriesName);
            seriesList.add(series);
        }
        series.addData(value);
        updateYRange();
    }

    /**
     * 清空所有数据
     */
    public void clear() {
        for (ChartSeries series : seriesList) {
            series.clear();
        }
        yMax = 1;
    }

    /**
     * 设置坐标轴标签
     *
     * @param xAxisLabel X轴标签
     * @param yAxisLabel Y轴标签
     */
    public void setAxisLabels(String xAxisLabel, String yAxisLabel) {
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
    }

    /**
     * 创建条形图画布
     *
     * @return 条形图画布实例
     */
    @Override
    /**
     * createCanvas方法。
     * @return ChartCanvas类型返回值
     */
    protected ChartCanvas createCanvas() {
        return new BarChartCanvas();
    }

    /**
     * 触发图表重绘
     */
    @Override
    /**
     * render方法。
     */
    public void render() {
        canvas.repaint();
    }

    /**
     * 更新Y轴范围
     */
    private void updateYRange() {
        yMax = 1;
        for (ChartSeries series : seriesList) {
            for (int i = 0; i < series.size(); i++) {
                yMax = Math.max(yMax, series.getData(i));
            }
        }
    }

    /**
     * 查找指定名称的数据系列
     *
     * @param name 系列名称
     * @return 找到的系列，不存在则返回null
     */
    private ChartSeries findSeries(String name) {
        for (ChartSeries series : seriesList) {
            if (series.getName().equals(name)) {
                return series;
            }
        }
        return null;
    }

    private class BarChartCanvas extends ChartCanvas {

        @Override
        /**
         * paintComponent方法。
         *      * @param g Graphics类型参数
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            int chartWidth = canvasWidth - PADDING - RIGHT_PADDING;
            int chartHeight = canvasHeight - TOP_PADDING - BOTTOM_PADDING;

            drawAxes(g2d, PADDING, TOP_PADDING, chartWidth, chartHeight);
            drawGrid(g2d, PADDING, TOP_PADDING, chartWidth, chartHeight);
            drawBars(g2d, PADDING, TOP_PADDING, chartWidth, chartHeight);
            drawLegend(g2d, PADDING, TOP_PADDING);
        }

        private void drawAxes(Graphics2D g2d, int x, int y, int width, int height) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(2));

            g2d.drawLine(x, y, x, y + height);
            g2d.drawLine(x, y + height, x + width, y + height);

            FontMetrics metrics = g2d.getFontMetrics();

            g2d.drawString(yAxisLabel, x - metrics.stringWidth(yAxisLabel) - 10, y - 10);
            g2d.drawString(xAxisLabel, x + width - metrics.stringWidth(xAxisLabel) + 10, y + height + 30);

            int tickCount = 5;
            for (int i = 0; i <= tickCount; i++) {
                double value = yMax * i / tickCount;
                int tickY = y + (int) (height * (1 - i / (double) tickCount));

                g2d.setColor(Color.GRAY);
                g2d.drawLine(x - 5, tickY, x, tickY);

                String tickLabel = String.format("%.1f", value);
                g2d.drawString(tickLabel, x - metrics.stringWidth(tickLabel) - 8, tickY + 5);
            }
        }

        private void drawGrid(Graphics2D g2d, int x, int y, int width, int height) {
            g2d.setColor(new Color(230, 230, 230));
            g2d.setStroke(new BasicStroke(1));

            int tickCount = 5;
            for (int i = 0; i <= tickCount; i++) {
                int tickY = y + (int) (height * i / tickCount);
                g2d.drawLine(x, tickY, x + width, tickY);
            }
        }

        private void drawBars(Graphics2D g2d, int x, int y, int width, int height) {
            if (seriesList.isEmpty()) return;

            ChartSeries series = seriesList.get(0);
            if (series.size() == 0) return;

            int groupCount = series.size();
            int groupWidth = width / (groupCount + 1);

            for (int i = 0; i < series.size(); i++) {
                double value = series.getData(i);
                int barHeight = (int) (height * value / yMax);
                int barX = x + (i + 1) * groupWidth - groupWidth / 3;
                int barY = y + height - barHeight;

                Color color = ChartColors.getColor(i);

                g2d.setColor(color);
                g2d.fillRect(barX, barY, groupWidth * 2 / 3, barHeight);

                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(barX, barY, groupWidth * 2 / 3, barHeight);

                // X轴标签
                FontMetrics metrics = g2d.getFontMetrics();
                String label = series.getLabel(i);
                int labelWidth = metrics.stringWidth(label);
                g2d.drawString(label, barX + groupWidth / 3 - labelWidth / 2, y + height + 25);
            }
        }

        private void drawLegend(Graphics2D g2d, int x, int y) {
            if (seriesList.size() <= 1) return;

            int legendY = 15;
            int legendX = x + 10;

            FontMetrics metrics = g2d.getFontMetrics();

            for (int i = 0; i < seriesList.size(); i++) {
                ChartSeries series = seriesList.get(i);
                Color color = ChartColors.getColor(i);

                g2d.setColor(color);
                g2d.fillRect(legendX, legendY - 8, 15, 10);

                g2d.setColor(Color.BLACK);
                g2d.drawString(series.getName(), legendX + 23, legendY);

                legendX += 25 + metrics.stringWidth(series.getName());
            }
        }
    }
}