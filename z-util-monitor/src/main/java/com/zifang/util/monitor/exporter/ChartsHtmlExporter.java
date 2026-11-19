package com.zifang.util.monitor.exporter;

import com.zifang.util.monitor.chart.ChartFormatter;
import com.zifang.util.monitor.chart.ChartSeries;
import com.zifang.util.monitor.chart.Sparkline;
import com.zifang.util.monitor.sampling.MetricDefinition;
import com.zifang.util.monitor.sampling.Monitor;
import com.zifang.util.monitor.sampling.Sample;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 趋势面板导出器。
 * <p>
 * 把一段时间内的采样画成一张表：每行一个指标，左侧是当前读数，右侧是一条迷你
 * 趋势图，点指标名可以打开完整折线图。
 * </p>
 *
 * @author zifang
 */
public class ChartsHtmlExporter {

    private static final String CSS =
            "<style>" +
                    "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }" +
                    ".container { max-width: 1200px; margin: 0 auto; }" +
                    "h1 { color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px; }" +
                    "h2 { color: #555; margin-top: 28px; font-size: 1.05em; }" +
                    "table { width: 100%; border-collapse: collapse; background: #fff; margin: 10px 0 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }" +
                    "th, td { padding: 8px 12px; text-align: left; border-bottom: 1px solid #eee; font-size: 0.92em; }" +
                    "th { background: #fafafa; color: #666; font-weight: 500; }" +
                    "td.value { font-family: 'Monaco', 'Menlo', monospace; text-align: right; white-space: nowrap; }" +
                    "td.spark { width: 220px; }" +
                    "a { color: #007bff; text-decoration: none; }" +
                    "a:hover { text-decoration: underline; }" +
                    ".toolbar { margin: 14px 0; color: #666; font-size: 0.9em; }" +
                    ".toolbar a { margin-right: 14px; }" +
                    ".footer { margin-top: 30px; padding-top: 16px; border-top: 1px solid #ddd; color: #888; font-size: 0.9em; }" +
                    "</style>";

    private final Monitor monitor;
    private final long windowMillis;

    /**
     * ChartsHtmlExporter方法。
     *
     * @param monitor       Monitor类型参数
     * @param windowMillis long类型参数
     */
    public ChartsHtmlExporter(Monitor monitor, long windowMillis) {
        this.monitor = monitor;
        this.windowMillis = windowMillis;
    }

    /**
     * export方法。
     *
     * @return String类型返回值
     */
    public String export() {
        long to = System.currentTimeMillis();
        long from = to - windowMillis;
        Map<String, ChartSeries> seriesMap = monitor.seriesAll(from, to);
        Sample latest = monitor.latest();

        Map<MetricDefinition.Category, List<MetricDefinition>> grouped = new LinkedHashMap<>();
        for (MetricDefinition definition : monitor.definitions()) {
            grouped.computeIfAbsent(definition.getCategory(), k -> new ArrayList<>()).add(definition);
        }

        StringBuilder html = new StringBuilder(8192);
        html.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Z-Util Monitor 趋势</title>\n");
        html.append(CSS);
        html.append("</head>\n<body>\n    <div class=\"container\">\n");
        html.append("        <h1>Z-Util Monitor 趋势</h1>\n");
        html.append("        <div class=\"toolbar\">窗口 ")
                .append(windowMillis / 1000).append(" 秒 · 样本 ")
                .append(monitor.query(from, to).size()).append(" 条 · ");
        html.append("<a href=\"/charts?window=").append(windowMillis / 1000).append("\">刷新</a>");
        html.append("<a href=\"/\">指标总览</a>");
        html.append("<a href=\"/json\">JSON</a></div>\n");

        for (Map.Entry<MetricDefinition.Category, List<MetricDefinition>> entry : grouped.entrySet()) {
            html.append("        <h2>").append(entry.getKey().getLabel()).append("</h2>\n");
            html.append("        <table>\n            <thead>\n                <tr>\n");
            html.append("                    <th>指标</th>\n                    <th>当前值</th>\n");
            html.append("                    <th>趋势</th>\n                </tr>\n");
            html.append("            </thead>\n            <tbody>\n");
            for (MetricDefinition definition : entry.getValue()) {
                ChartSeries series = seriesMap.get(definition.getName());
                String sparkline = series == null ? "" : Sparkline.render(series, 200, 32);
                html.append("                <tr>\n");
                html.append("                    <td><a href=\"/chart.svg?metric=")
                        .append(escapeHtml(definition.getName()))
                        .append("&window=").append(windowMillis / 1000).append("\">")
                        .append(escapeHtml(definition.getName())).append("</a></td>\n");
                html.append("                    <td class=\"value\">")
                        .append(escapeHtml(currentValue(latest, definition))).append("</td>\n");
                html.append("                    <td class=\"spark\">").append(sparkline).append("</td>\n");
                html.append("                </tr>\n");
            }
            html.append("            </tbody>\n        </table>\n");
        }

        html.append("        <div class=\"footer\">Generated by Z-Util Monitor at ")
                .append(ChartFormatter.dateTime(System.currentTimeMillis())).append("</div>\n");
        html.append("    </div>\n</body>\n</html>");
        return html.toString();
    }

    private String currentValue(Sample latest, MetricDefinition definition) {
        if (latest == null) {
            return "-";
        }
        Number value = latest.get(definition.getName());
        if (value == null) {
            return "-";
        }
        return ChartFormatter.format(value.doubleValue(), definition.getUnit());
    }

    private String escapeHtml(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
