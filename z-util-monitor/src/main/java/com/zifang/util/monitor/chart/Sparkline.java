package com.zifang.util.monitor.chart;

import java.util.List;

/**
 * 迷你趋势图。
 * <p>
 * 用于表格里每行指标后面的内嵌小图：不带坐标轴与图例，只有折线与填充，
 * 宽度通常只有一百多像素。
 * </p>
 *
 * @author zifang
 */
public final class Sparkline {

    private Sparkline() {
    }

    /**
     * 渲染迷你图。
     *
     * @param series ChartSeries类型参数
     * @param width  int类型参数
     * @param height int类型参数
     *
     * @return String类型返回值
     */
    public static String render(ChartSeries series, int width, int height) {
        if (series == null || series.isEmpty() || width <= 0 || height <= 0) {
            return "";
        }
        List<ChartSeries.Point> points = series.getPoints();
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (ChartSeries.Point p : points) {
            double value = p.getValue();
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                continue;
            }
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        if (min > max) {
            return "";
        }
        if (max - min < 1e-9) {
            max = min + 1;
        }

        long from = points.get(0).getTimestamp();
        long to = points.get(points.size() - 1).getTimestamp();
        if (to <= from) {
            to = from + 1;
        }

        StringBuilder coords = new StringBuilder();
        int drawn = 0;
        for (ChartSeries.Point p : points) {
            double value = p.getValue();
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                continue;
            }
            double xRatio = (double) (p.getTimestamp() - from) / (to - from);
            double yRatio = (value - min) / (max - min);
            int x = (int) Math.round(xRatio * (width - 1));
            int y = height - 1 - (int) Math.round(yRatio * (height - 1));
            if (drawn > 0) {
                coords.append(' ');
            }
            coords.append(x).append(',').append(y);
            drawn++;
        }
        if (drawn == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width)
                .append("\" height=\"").append(height)
                .append("\" viewBox=\"0 0 ").append(width).append(' ').append(height)
                .append("\" preserveAspectRatio=\"none\">");
        sb.append("<polyline fill=\"none\" stroke=\"#007bff\" stroke-width=\"1.2\"")
                .append(" stroke-linejoin=\"round\" stroke-linecap=\"round\" points=\"")
                .append(coords).append("\"/>");
        sb.append("</svg>");
        return sb.toString();
    }
}
