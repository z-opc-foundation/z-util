package com.zifang.util.monitor.sampling;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 一次采样快照。
 * <p>
 * 采用「宽表」结构：一个 tick 一行，行内是 <em>指标名 → 数值</em> 的扁平映射。
 * 这样做的好处有两个：按单个指标绘图时只需扫一行即可取到全部点；落盘时一行就是一条
 * JSON 记录，既不用拼字段名也不用做列对齐。
 * </p>
 * <p>
 * 数值型指标放 {@code values}，文本型信息（主机名、网卡 IP、JVM 启动参数等）放
 * {@code attributes} —— 后者只用于展示，不参与绘图。
 * </p>
 *
 * @author zifang
 */
public final class Sample {

    /**
     * 单行样本的粗略字节开销估算基数（对象头 + Map 节点 + 键引用）。
     */
    private static final int ENTRY_OVERHEAD_BYTES = 48;
    /**
     * 文本属性单条的粗略字节开销估算。
     */
    private static final int ATTRIBUTE_OVERHEAD_BYTES = 96;

    private final long timestamp;
    private final Map<String, Number> values;
    private final Map<String, String> attributes;

    /**
     * Sample方法。
     *
     * @param timestamp  long类型参数
     * @param values     MapStringNumber类型参数
     * @param attributes MapStringString类型参数
     */
    public Sample(long timestamp, Map<String, Number> values, Map<String, String> attributes) {
        this.timestamp = timestamp;
        this.values = values == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(values));
        this.attributes = attributes == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    /**
     * 创建一个空样本构建器。
     *
     * @param timestamp long类型参数
     *
     * @return Builder类型返回值
     */
    public static Builder builder(long timestamp) {
        return new Builder(timestamp);
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
     * getValues方法。
     *
     * @return MapStringNumber类型返回值
     */
    public Map<String, Number> getValues() {
        return values;
    }

    /**
     * getAttributes方法。
     *
     * @return MapStringString类型返回值
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * 取单个指标值。
     *
     * @param metric String类型参数
     *
     * @return Number类型返回值，缺失时返回 null
     */
    public Number get(String metric) {
        return values.get(metric);
    }

    /**
     * 取单个指标值，缺失时返回默认值。
     *
     * @param metric       String类型参数
     * @param defaultValue double类型参数
     *
     * @return double类型返回值
     */
    public double getDouble(String metric, double defaultValue) {
        Number value = values.get(metric);
        return value == null ? defaultValue : value.doubleValue();
    }

    /**
     * 本行包含的指标名。
     *
     * @return SetString类型返回值
     */
    public Set<String> metrics() {
        return values.keySet();
    }

    /**
     * 本行的粗略内存占用估算，供按容量淘汰使用。
     *
     * @return long类型返回值
     */
    public long estimatedBytes() {
        return 64L
                + (long) values.size() * ENTRY_OVERHEAD_BYTES
                + (long) attributes.size() * ATTRIBUTE_OVERHEAD_BYTES;
    }

    @Override
    public String toString() {
        return "Sample{timestamp=" + timestamp + ", metrics=" + values.size()
                + ", attributes=" + attributes.size() + '}';
    }

    /**
     * 样本构建器。
     */
    public static final class Builder {

        private final long timestamp;
        private final Map<String, Number> values = new LinkedHashMap<>();
        private final Map<String, String> attributes = new LinkedHashMap<>();

        private Builder(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * 写入一个数值指标。
         *
         * @param metric String类型参数
         * @param value  double类型参数
         *
         * @return Builder类型返回值
         */
        public Builder put(String metric, double value) {
            if (metric != null && !Double.isNaN(value) && !Double.isInfinite(value)) {
                values.put(metric, value);
            }
            return this;
        }

        /**
         * 写入一个数值指标。
         *
         * @param metric String类型参数
         * @param value  long类型参数
         *
         * @return Builder类型返回值
         */
        public Builder put(String metric, long value) {
            if (metric != null) {
                values.put(metric, value);
            }
            return this;
        }

        /**
         * 写入一个文本属性。
         *
         * @param key   String类型参数
         * @param value String类型参数
         *
         * @return Builder类型返回值
         */
        public Builder attribute(String key, String value) {
            if (key != null && value != null) {
                attributes.put(key, value);
            }
            return this;
        }

        /**
         * build方法。
         *
         * @return Sample类型返回值
         */
        public Sample build() {
            return new Sample(timestamp, values, attributes);
        }
    }
}
