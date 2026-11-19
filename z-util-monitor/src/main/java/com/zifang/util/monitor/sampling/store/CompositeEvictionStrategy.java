package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;

import java.util.Arrays;
import java.util.List;

/**
 * 组合淘汰策略。
 * <p>
 * 把多个策略依次作用在样本上，任一策略要求淘汰就淘汰。由于每个策略淘汰的都是
 * 「最旧的一段前缀」，多个前缀的并集就是最长的那个前缀，因此合并结果直接取各
 * 策略要求的最大值 —— 无需复制样本列表，采样路径上零分配。
 * </p>
 *
 * @author zifang
 */
public final class CompositeEvictionStrategy implements EvictionStrategy {

    private final List<EvictionStrategy> strategies;

    /**
     * CompositeEvictionStrategy方法。
     *
     * @param strategies ListEvictionStrategy类型参数
     */
    public CompositeEvictionStrategy(List<EvictionStrategy> strategies) {
        this.strategies = Arrays.asList(strategies.toArray(new EvictionStrategy[0]));
    }

    /**
     * 默认组合：条数、时效、容量三重上限。
     *
     * @return CompositeEvictionStrategy类型返回值
     */
    public static CompositeEvictionStrategy defaultStrategy() {
        return new CompositeEvictionStrategy(Arrays.asList(
                new FifoEvictionStrategy(),
                new TtlEvictionStrategy(),
                new CapacityEvictionStrategy()));
    }

    /**
     * getStrategies方法。
     *
     * @return ListEvictionStrategy类型返回值
     */
    public List<EvictionStrategy> getStrategies() {
        return strategies;
    }

    @Override
    public int selectEvictionCount(List<Sample> samples, EvictionPolicy policy, long now) {
        int max = 0;
        for (EvictionStrategy strategy : strategies) {
            int count = strategy.selectEvictionCount(samples, policy, now);
            if (count > max) {
                max = count;
            }
        }
        return Math.min(max, samples.size());
    }
}
