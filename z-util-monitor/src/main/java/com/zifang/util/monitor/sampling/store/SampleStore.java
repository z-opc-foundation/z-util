package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;

import java.util.List;

/**
 * 采样存储。
 * <p>
 * 实现方可以是内存环形缓冲（{@link InMemorySampleStore}），也可以是磁盘文件
 * （{@link FileSampleStore}），甚至两者串联。{@link com.zifang.util.monitor.sampling.Monitor}
 * 只依赖本接口，因此换存储不需要改采集与绘图代码。
 * </p>
 *
 * @author zifang
 */
public interface SampleStore extends AutoCloseable {

    /**
     * 追加一条样本。
     *
     * @param sample Sample类型参数
     */
    void append(Sample sample);

    /**
     * 查询时间区间内的样本，两端均为闭区间。
     *
     * @param from long类型参数
     * @param to   long类型参数
     *
     * @return ListSample类型返回值
     */
    List<Sample> query(long from, long to);

    /**
     * 查询全部样本。
     *
     * @return ListSample类型返回值
     */
    List<Sample> query();

    /**
     * 最新一条样本。
     *
     * @return Sample类型返回值，无数据时返回 null
     */
    Sample latest();

    /**
     * 清空存储。
     */
    void clear();

    /**
     * 当前保留的样本条数。
     *
     * @return int类型返回值
     */
    int size();

    /**
     * 当前占用的字节数估算。
     *
     * @return long类型返回值
     */
    long estimatedBytes();

    /**
     * 存储统计。
     *
     * @return StoreStats类型返回值
     */
    StoreStats stats();

    /**
     * 释放资源。
     */
    @Override
    void close();
}
