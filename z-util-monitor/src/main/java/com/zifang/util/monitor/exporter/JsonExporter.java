package com.zifang.util.monitor.exporter;

import com.zifang.util.core.lang.collection.Lists;
import com.zifang.util.monitor.MetricsRegistry;
import com.zifang.util.monitor.MetricsSnapshot;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * JSON 指标导出器
 */
public class JsonExporter {

    private final MetricsRegistry registry;

    /**
     * JsonExporter方法。
     * * @param registry MetricsRegistry类型参数
     */
    public JsonExporter(MetricsRegistry registry) {
        this.registry = registry;
    }

    /**
     * export方法。
     *
     * @return String类型返回值
     */
    public String export() {
        List<MetricsSnapshot> snapshots = registry.collect();

        // 按分类分组
        Map<MetricsSnapshot.Category, List<MetricsSnapshot>> grouped = new TreeMap<>();
        for (MetricsSnapshot snapshot : snapshots) {
            grouped.computeIfAbsent(snapshot.getCategory(), k -> new java.util.ArrayList<>()).add(snapshot);
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
        json.append("  \"total\": ").append(snapshots.size()).append(",\n");

        // 分类汇总
        json.append("  \"categories\": {\n");
        boolean firstCategory = true;
        for (MetricsSnapshot.Category category : MetricsSnapshot.Category.values()) {
            int count = grouped.getOrDefault(category, Lists.of()).size();
            if (count > 0) {
                if (!firstCategory) {
                    json.append(",\n");
                }
                json.append("    \"").append(category.name()).append("\": ").append(count);
                firstCategory = false;
            }
        }
        json.append("\n  },\n");

        // 详细指标
        json.append("  \"metrics\": [\n");
        boolean firstMetric = true;
        for (MetricsSnapshot snapshot : snapshots) {
            if (!firstMetric) {
                json.append(",\n");
            }
            json.append("    {\n");
            json.append("      \"name\": \"").append(escapeJson(snapshot.getName())).append("\",\n");
            json.append("      \"value\": ").append(formatValue(snapshot)).append(",\n");
            json.append("      \"category\": \"").append(snapshot.getCategory().name()).append("\",\n");
            if (snapshot.getUnit() != null) {
                json.append("      \"unit\": \"").append(escapeJson(snapshot.getUnit())).append("\",\n");
            }
            if (snapshot.getDescription() != null) {
                json.append("      \"description\": \"").append(escapeJson(snapshot.getDescription())).append("\",\n");
            }
            json.append("      \"timestamp\": ").append(snapshot.getTimestamp()).append("\n");
            json.append("    }");
            firstMetric = false;
        }
        json.append("\n  ]\n");

        json.append("}");
        return json.toString();
    }

    private String formatValue(MetricsSnapshot snapshot) {
        Object value = snapshot.getValue();
        if (value == null) {
            return "null";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        return "\"" + escapeJson(value.toString()) + "\"";
    }

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
