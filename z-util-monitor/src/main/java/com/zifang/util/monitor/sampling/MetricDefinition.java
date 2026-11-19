package com.zifang.util.monitor.sampling;

/**
 * 指标定义。
 * <p>
 * 描述一个被采集指标的元信息：名字、分类、单位、类型与释义。
 * 名字在全局唯一，是采样存储、查询与绘图的唯一键。
 * </p>
 *
 * @author zifang
 */
public final class MetricDefinition {

    private final String name;
    private final Category category;
    private final String unit;
    private final Type type;
    private final String description;

    /**
     * MetricDefinition方法。
     *
     * @param name        String类型参数
     * @param category    Category类型参数
     * @param unit        String类型参数
     * @param type        Type类型参数
     * @param description String类型参数
     */
    public MetricDefinition(String name, Category category, String unit, Type type, String description) {
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.type = type;
        this.description = description;
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
     * getCategory方法。
     *
     * @return Category类型返回值
     */
    public Category getCategory() {
        return category;
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
     * getType方法。
     *
     * @return Type类型返回值
     */
    public Type getType() {
        return type;
    }

    /**
     * getDescription方法。
     *
     * @return String类型返回值
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "MetricDefinition{name='" + name + "', category=" + category
                + ", unit='" + unit + "', type=" + type + '}';
    }

    /**
     * 指标分类。
     */
    public enum Category {
        JVM("JVM"),
        HOST("宿主机"),
        DISK("磁盘"),
        NET("网络"),
        PROCESS("进程"),
        CUSTOM("自定义");

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

    /**
     * 指标类型。
     */
    public enum Type {
        /**
         * 瞬时值：每次采样读到的当前状态，如堆内存使用量。
         */
        GAUGE,
        /**
         * 累计值：只增不减的计数器，如 GC 次数、网卡累计收发字节。
         * 绘图时应换算成速率才有意义。
         */
        COUNTER
    }
}
