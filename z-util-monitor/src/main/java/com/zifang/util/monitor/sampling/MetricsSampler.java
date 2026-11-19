package com.zifang.util.monitor.sampling;

import java.util.Collection;

/**
 * 指标采集器。
 * <p>
 * 一个采集器负责一族相关的指标（JVM、宿主机、磁盘、网络…），实现方只需声明
 * 自己会产出哪些指标，并在采样时把数值写进 {@link SampleSink}。
 * </p>
 * <p>
 * 实现方应当是<b>有状态但线程安全</b>的：采集器内部可能需要记住上一次的累计值，
 * 以便换算速率。
 * </p>
 *
 * @author zifang
 */
public interface MetricsSampler {

    /**
     * 采集器标识，全局唯一，用于日志与查询过滤。
     *
     * @return String类型返回值
     */
    String id();

    /**
     * 本采集器会产出的指标定义。
     *
     * @return CollectionMetricDefinition类型返回值
     */
    Collection<MetricDefinition> definitions();

    /**
     * 执行一次采样，把数值写入槽中。
     *
     * @param sink SampleSink类型参数
     */
    void sample(SampleSink sink);
}
