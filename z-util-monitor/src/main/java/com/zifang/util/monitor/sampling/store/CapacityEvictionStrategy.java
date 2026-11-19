package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;

import java.util.List;

/**
 * 容量淘汰策略。
 * <p>
 * 按样本的字节占用估算控制总量，超预算就从最旧一端丢弃。
 * 条数上限失灵时（例如某些采样周期产出异常多的指标），这条是兜底。
 * </p>
 *
 * @author zifang
 */
public final class CapacityEvictionStrategy implements EvictionStrategy {

    /**
     * CapacityEvictionStrategy方法。
     */
    public CapacityEvictionStrategy() {
    }

    @Override
    public int selectEvictionCount(List<Sample> samples, EvictionPolicy policy, long now) {
        long maxBytes = policy.getMaxBytes();
        if (maxBytes <= 0) {
            return 0;
        }
        long total = 0;
        for (Sample sample : samples) {
            total += sample.estimatedBytes();
        }
        int count = 0;
        while (total > maxBytes && count < samples.size()) {
            total -= samples.get(count).estimatedBytes();
            count++;
        }
        return count;
    }
}
