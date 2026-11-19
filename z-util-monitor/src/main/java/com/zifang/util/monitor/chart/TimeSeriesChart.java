package com.zifang.util.monitor.chart;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端时序折线图。
 * <p>
 * 直接输出 SVG 字符串，不依赖任何图表库，也不依赖浏览器端的 JS 库 —— 监控页只要
 * 把这段字符串塞进 {@code <img src="...chart.svg">} 或者直接内联即可显示。
 * </p>
 * <p>
 * 坐标系：横轴为时间，纵轴为数值（多条序列共享同一纵轴范围）。
 * </p>
 *
 * @author zifang
 */
public final class TimeSeriesChart {

    private static final String[] PALETTE = {
            "#007bff", "#28a745", "#dc3545", "#fd7e14", "#6f42c1", "#17a2b8", "#20c997", "#e83e8c"
    };

    private static final int MARGIN_LEFT = 72;
    private static final int MARGIN_RIGHT = 18;
    private static final int MARGIN_TOP = 44;
    private static final int MARGIN_BOTTOM = 34;

    private int width = 820;
    private int height = 280;
    private String title = "";
    private String unit;
    private long from;
    private long to;
    private final List<ChartSeries> series = new ArrayList<>();

    /**
     * 创建构建器。
     *
     * @return Builder类型返回值
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 渲染为 SVG。
     *
     * @return String类型返回值
     */
    public String toSvg() {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width)
                .append("\" height=\"").append(height)
                .append("\" viewBox=\"0 0 ").append(width).append(' ').append(height)
                .append("\" role=\"img\">\n");
        sb.append("  <rect width=\"").append(width).append("\" height=\"").append(height)
                .append("\" fill=\"#ffffff\"/>\n");

        if (title != null && !title.isEmpty()) {
            sb.append("  <text x=\"").append(MARGIN_LEFT).append("\" y=\"22\" font-family=\"-apple-system,Segoe UI,Roboto,sans-serif\"")
                    .append(" font-size=\"14\" font-weight=\"600\" fill=\"#333\">")
                    .append(escape(title)).append("</text>\n");
        }

        int plotWidth = Math.max(1, width - MARGIN_LEFT - MARGIN_RIGHT);
        int plotHeight = Math.max(1, height - MARGIN_TOP - MARGIN_BOTTOM);

        boolean hasData = false;
        for (ChartSeries s : series) {
            if (!s.isEmpty()) {
                hasData = true;
                break;
            }
        }
        if (!hasData) {
            sb.append("  <text x=\"").append(MARGIN_LEFT + plotWidth / 2)
                    .append("\" y=\"").append(MARGIN_TOP + plotHeight / 2)
                    .append("\" text-anchor=\"middle\" font-family=\"-apple-system,Segoe UI,Roboto,sans-serif\"")
                    .append(" font-size=\"13\" fill=\"#999\">无数据</text>\n");
            sb.append("</svg>\n");
            return sb.toString();
        }

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        long minTs = Long.MAX_VALUE;
        long maxTs = Long.MIN_VALUE;
        for (ChartSeries s : series) {
            for (ChartSeries.Point p : s.getPoints()) {
                if (Double.isNaN(p.getValue()) || Double.isInfinite(p.getValue())) {
                    continue;
                }
                min = Math.min(min, p.getValue());
                max = Math.max(max, p.getValue());
                minTs = Math.min(minTs, p.getTimestamp());
                maxTs = Math.max(maxTs, p.getTimestamp());
            }
        }
        if (min > max) {
            min = 0;
            max = 1;
        }
        if (max - min < 1e-9) {
            double pad = Math.max(1.0, Math.abs(max) * 0.05);
            min -= pad;
            max += pad;
        } else {
            double pad = (max - min) * 0.05;
            min -= pad;
            max += pad;
        }

        long rangeFrom = from;
        long rangeTo = to;
        if (rangeTo <= rangeFrom) {
            rangeFrom = minTs;
            rangeTo = maxTs;
            if (rangeTo <= rangeFrom) {
                rangeTo = rangeFrom + 1000;
            }
        }
        final long finalFrom = rangeFrom;
        final long finalTo = rangeTo;
        final double finalMin = min;
        final double finalMax = max;

        // 网格与纵轴刻度
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            double ratio = (double) i / gridLines;
            int y = MARGIN_TOP + plotHeight - (int) Math.round(ratio * plotHeight);
            sb.append("  <line x1=\"").append(MARGIN_LEFT).append("\" y1=\"").append(y)
                    .append("\" x2=\"").append(MARGIN_LEFT + plotWidth).append("\" y2=\"").append(y)
                    .append("\" stroke=\"#eceff1\" stroke-width=\"1\"/>\n");
            double value = finalMax - ratio * (finalMax - finalMin);
            sb.append("  <text x=\"").append(MARGIN_LEFT - 8).append("\" y=\"").append(y + 4)
                    .append("\" text-anchor=\"end\" font-family=\"-apple-system,Segoe UI,Roboto,sans-serif\"")
                    .append(" font-size=\"11\" fill=\"#777\">")
                    .append(escape(ChartFormatter.format(value, unit))).append("</text>\n");
        }

        // 横轴
        sb.append("  <line x1=\"").append(MARGIN_LEFT).append("\" y1=\"").append(MARGIN_TOP + plotHeight)
                .append("\" x2=\"").append(MARGIN_LEFT + plotWidth).append("\" y2=\"").append(MARGIN_TOP + plotHeight)
                .append("\" stroke=\"#cfd8dc\" stroke-width=\"1\"/>\n");
        int xTicks = 4;
        for (int i = 0; i <= xTicks; i++) {
            double ratio = (double) i / xTicks;
            int x = MARGIN_LEFT + (int) Math.round(ratio * plotWidth);
            long ts = (long) (finalFrom + ratio * (finalTo - finalFrom));
            String anchor = i == 0 ? "start" : (i == xTicks ? "end" : "middle");
            sb.append("  <text x=\"").append(x).append("\" y=\"").append(MARGIN_TOP + plotHeight + 16)
                    .append("\" text-anchor=\"").append(anchor)
                    .append("\" font-family=\"-apple-system,Segoe UI,Roboto,sans-serif\"")
                    .append(" font-size=\"11\" fill=\"#777\">")
                    .append(escape(ChartFormatter.time(ts))).append("</text>\n");
        }

        // 数据折线
        for (int s = 0; s < series.size(); s++) {
            ChartSeries current = series.get(s);
            String color = PALETTE[s % PALETTE.length];
            StringBuilder points = new StringBuilder();
            int drawn = 0;
            for (ChartSeries.Point p : current.getPoints()) {
                if (p.getTimestamp() < finalFrom || p.getTimestamp() > finalTo) {
                    continue;
                }
                double value = p.getValue();
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    continue;
                }
                double xRatio = (double) (p.getTimestamp() - finalFrom) / (finalTo - finalFrom);
                double yRatio = (value - finalMin) / (finalMax - finalMin);
                int x = MARGIN_LEFT + (int) Math.round(xRatio * plotWidth);
                int y = MARGIN_TOP + plotHeight - (int) Math.round(yRatio * plotHeight);
                if (drawn > 0) {
                    points.append(' ');
                }
                points.append(x).append(',').append(y);
                drawn++;
            }
            if (drawn == 0) {
                continue;
            }
            if (drawn == 1) {
                String[] xy = points.toString().split(",");
                sb.append("  <circle cx=\"").append(xy[0]).append("\" cy=\"").append(xy[1])
                        .append("\" r=\"2.5\" fill=\"").append(color).append("\"/>\n");
            } else {
                sb.append("  <polyline fill=\"none\" stroke=\"").append(color)
                        .append("\" stroke-width=\"1.6\" stroke-linejoin=\"round\" stroke-linecap=\"round\"")
                        .append(" points=\"").append(points).append("\"/>\n");
            }
        }

        // 图例
        int legendY = 40;
        int legendX = MARGIN_LEFT;
        for (int s = 0; s < series.size(); s++) {
            ChartSeries current = series.get(s);
            if (current.isEmpty()) {
                continue;
            }
            String color = PALETTE[s % PALETTE.length];
            sb.append("  <rect x=\"").append(legendX).append("\" y=\"").append(legendY - 8)
                    .append("\" width=\"10\" height=\"10\" rx=\"2\" fill=\"").append(color).append("\"/>\n");
            String label = current.getName();
            sb.append("  <text x=\"").append(legendX + 15).append("\" y=\"").append(legendY)
                    .append("\" font-family=\"-apple-system,Segoe UI,Roboto,sans-serif\"")
                    .append(" font-size=\"11\" fill=\"#555\">").append(escape(label)).append("</text>\n");
            legendX += 22 + label.length() * 7;
        }

        sb.append("</svg>\n");
        return sb.toString();
    }

    /**
     * XML 文本转义。
     *
     * @param text String类型参数
     *
     * @return String类型返回值
     */
    static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 图表构建器。
     */
    public static final class Builder {

        private final TimeSeriesChart chart = new TimeSeriesChart();

        /**
         * width方法。
         *
         * @param width int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder width(int width) {
            if (width > 0) {
                chart.width = width;
            }
            return this;
        }

        /**
         * height方法。
         *
         * @param height int类型参数
         *
         * @return Builder类型返回值
         */
        public Builder height(int height) {
            if (height > 0) {
                chart.height = height;
            }
            return this;
        }

        /**
         * title方法。
         *
         * @param title String类型参数
         *
         * @return Builder类型返回值
         */
        public Builder title(String title) {
            chart.title = title;
            return this;
        }

        /**
         * unit方法。
         *
         * @param unit String类型参数
         *
         * @return Builder类型返回值
         */
        public Builder unit(String unit) {
            chart.unit = unit;
            return this;
        }

        /**
         * range方法。
         *
         * @param from long类型参数
         * @param to   long类型参数
         *
         * @return Builder类型返回值
         */
        public Builder range(long from, long to) {
            chart.from = from;
            chart.to = to;
            return this;
        }

        /**
         * series方法。
         *
         * @param series ChartSeries类型参数
         *
         * @return Builder类型返回值
         */
        public Builder series(ChartSeries series) {
            if (series != null) {
                chart.series.add(series);
            }
            return this;
        }

        /**
         * build方法。
         *
         * @return TimeSeriesChart类型返回值
         */
        public TimeSeriesChart build() {
            return chart;
        }
    }
}
