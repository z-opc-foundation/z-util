package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;

import java.util.List;

/**
 * 时效淘汰策略。
 * <p>
 * 丢弃早于保留窗口的样本。适合「只看最近 N 分钟」的场景：即使采样停了，
 * 旧数据也不会一直占着内存。
 * </p>
 *
 * @author zifang
 */
public final class TtlEvictionStrategy implements EvictionStrategy {

    /**
     * TtlEvictionStrategy方法。
     */
    public TtlEvictionStrategy() {
    }

    @Override
    public int selectEvictionCount(List<Sample> samples, EvictionPolicy policy, long now) {
        long maxAgeMillis = policy.getMaxAgeMillis();
        if (maxAgeMillis <= 0) {
            return 0;
        }
        long deadline = now - maxAgeMillis;
        int count = 0;
        for (Sample sample : samples) {
            if (sample.getTimestamp() < deadline) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
