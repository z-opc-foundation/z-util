package com.zifang.util.monitor;

/**
 * 指标快照数据模型
 * 用于存储单个时间点的指标数据
 */
public class MetricsSnapshot {

    private final long timestamp;
    private final String name;
    private final Object value;
    private final String description;
    private final String unit;
    private final Category category;

    /**
     * MetricsSnapshot方法。
     * * @param name String类型参数
     *
     * @param value Object类型参数
     */
    public MetricsSnapshot(String name, Object value) {
        this(name, value, null, null, null);
    }

    /**
     * MetricsSnapshot方法。
     * * @param name String类型参数
     *
     * @param value       Object类型参数
     * @param description String类型参数
     * @param unit        String类型参数
     */
    public MetricsSnapshot(String name, Object value, String description, String unit) {
        this(name, value, description, unit, null);
    }

    /**
     * MetricsSnapshot方法。
     * * @param name String类型参数
     *
     * @param value       Object类型参数
     * @param description String类型参数
     * @param unit        String类型参数
     * @param category    Category类型参数
     */
    public MetricsSnapshot(String name, Object value, String description, String unit, Category category) {
        this.timestamp = System.currentTimeMillis();
        this.name = name;
        this.value = value;
        this.description = description;
        this.unit = unit;
        this.category = category;
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
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getValue方法。
     *
     * @return Object类型返回值
     */
    public Object getValue() {
        return value;
    }

    /**
     * getDescription方法。
     *
     * @return String类型返回值
     */
    public String getDescription() {
        return description;
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
     * getCategory方法。
     *
     * @return MetricsSnapshot.Category类型返回值
     */
    public MetricsSnapshot.Category getCategory() {
        return category;
    }

    /**
     * 指标分类
     */
    public enum Category {
        JVM("JVM 指标"),
        THREAD("线程指标"),
        OS("操作系统指标"),
        CUSTOM("自定义指标");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        /**
         * getLabel方法。
         *
         * @return String类型返回值
         */
        public String getLabel() {
            return label;
        }
    }
}
