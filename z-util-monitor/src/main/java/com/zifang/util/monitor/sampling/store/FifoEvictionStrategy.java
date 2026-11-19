package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;

import java.util.List;

/**
 * 条数淘汰策略。
 * <p>
 * 保留最新的 N 条，超出部分从最旧一端丢弃。
 * </p>
 *
 * @author zifang
 */
public final class FifoEvictionStrategy implements EvictionStrategy {

    /**
     * FifoEvictionStrategy方法。
     */
    public FifoEvictionStrategy() {
    }

    @Override
    public int selectEvictionCount(List<Sample> samples, EvictionPolicy policy, long now) {
        int maxSamples = policy.getMaxSamples();
        if (maxSamples <= 0) {
            return 0;
        }
        return Math.max(0, samples.size() - maxSamples);
    }
}
