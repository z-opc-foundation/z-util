package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;

import java.util.List;

/**
 * 淘汰策略。
 * <p>
 * 决定「在什么情况下、从哪一端、淘汰多少条样本」。默认实现只从最旧一端淘汰，
 * 保证时间序列始终连续 —— 这也是短周期绘图能直接画出折线的前提。
 * </p>
 * <p>
 * <b>契约</b>：实现方淘汰的样本集合必须是列表的一个前缀（即从最旧一端连续的一段）。
 * 组合策略正是依赖这一点把多个策略合并成「取最长的那个前缀」。
 * </p>
 *
 * @author zifang
 */
public interface EvictionStrategy {

    /**
     * 计算需要淘汰的样本条数。
     *
     * @param samples 当前样本，最旧的一条在列表头部
     * @param policy  淘汰口径
     * @param now     当前时间戳
     *
     * @return int类型返回值，需要从最旧一端淘汰的条数
     */
    int selectEvictionCount(List<Sample> samples, EvictionPolicy policy, long now);
}
