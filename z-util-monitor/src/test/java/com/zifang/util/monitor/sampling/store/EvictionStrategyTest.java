package com.zifang.util.monitor.sampling.store;

import com.zifang.util.monitor.sampling.Sample;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 淘汰策略测试。
 */
class EvictionStrategyTest {

    private static Sample sampleAt(long timestamp) {
        return Sample.builder(timestamp).put("m", 1).build();
    }

    private static List<Sample> samples(long... timestamps) {
        List<Sample> list = new ArrayList<>();
        for (long timestamp : timestamps) {
            list.add(sampleAt(timestamp));
        }
        return list;
    }

    @Test
    /**
     * testFifoKeepsLatest方法。
     */
    void testFifoKeepsLatest() {
        FifoEvictionStrategy strategy = new FifoEvictionStrategy();
        EvictionPolicy policy = new EvictionPolicy(3, 0, 0);

        assertEquals(2, strategy.selectEvictionCount(samples(1, 2, 3, 4, 5), policy, 5));
        assertEquals(0, strategy.selectEvictionCount(samples(1, 2, 3), policy, 3));
    }

    @Test
    /**
     * testTtlDropsExpired方法。
     */
    void testTtlDropsExpired() {
        TtlEvictionStrategy strategy = new TtlEvictionStrategy();
        EvictionPolicy policy = new EvictionPolicy(0, 1000, 0);

        // now=5000，窗口 1000ms ⇒ 早于 4000 的过期
        assertEquals(3, strategy.selectEvictionCount(samples(1000, 2000, 3999, 4000, 5000), policy, 5000));
        assertEquals(0, strategy.selectEvictionCount(samples(4000, 5000), policy, 5000));
    }

    @Test
    /**
     * testCapacityDropsOldest方法。
     */
    void testCapacityDropsOldest() {
        CapacityEvictionStrategy strategy = new CapacityEvictionStrategy();
        List<Sample> list = samples(1, 2, 3, 4);
        long oneSample = list.get(0).estimatedBytes();
        EvictionPolicy policy = new EvictionPolicy(0, 0, oneSample * 2);

        assertEquals(2, strategy.selectEvictionCount(list, policy, 4));
    }

    @Test
    /**
     * testCompositeTakesLongestPrefix方法。
     */
    void testCompositeTakesLongestPrefix() {
        CompositeEvictionStrategy strategy = CompositeEvictionStrategy.defaultStrategy();
        List<Sample> list = samples(1000, 2000, 3000, 4000, 5000);

        // 条数上限 2 → 淘汰 3 条
        assertEquals(3, strategy.selectEvictionCount(list, new EvictionPolicy(2, 0, 0), 5000));
        // 时效窗口 1000ms、now=5000 → 早于 4000 的三条过期
        assertEquals(3, strategy.selectEvictionCount(list, new EvictionPolicy(0, 1000, 0), 5000));
        // 两个都不限制
        assertEquals(0, strategy.selectEvictionCount(list, EvictionPolicy.unlimited(), 5000));
    }

    @Test
    /**
     * testUnlimitedPolicyKeepsEverything方法。
     */
    void testUnlimitedPolicyKeepsEverything() {
        CompositeEvictionStrategy strategy = CompositeEvictionStrategy.defaultStrategy();
        assertEquals(0, strategy.selectEvictionCount(samples(1, 2, 3), EvictionPolicy.unlimited(), 3));
        assertTrue(strategy.getStrategies().size() == 3);
    }
}
