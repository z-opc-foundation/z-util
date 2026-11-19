package com.zifang.util.monitor.sampling;

/**
 * 采样写入槽。
 * <p>
 * 采集器只管往里写，写到哪里（内存、磁盘、两者都写）由 {@link Monitor} 决定。
 * </p>
 *
 * @author zifang
 */
@FunctionalInterface
public interface SampleSink {

    /**
     * 写入一个数值指标。
     *
     * @param metric String类型参数
     * @param value  double类型参数
     */
    void put(String metric, double value);

    /**
     * 写入一个文本属性，默认忽略。
     *
     * @param key   String类型参数
     * @param value String类型参数
     */
    default void attribute(String key, String value) {
        // 默认不采集文本属性
    }
}
